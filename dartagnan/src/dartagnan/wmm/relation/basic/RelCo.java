package dartagnan.wmm.relation.basic;

import com.google.common.collect.ImmutableSetMultimap;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.IntExpr;
import dartagnan.program.event.Event;
import dartagnan.program.event.MemEvent;
import dartagnan.program.memory.Address;
import dartagnan.program.memory.Location;
import dartagnan.program.utils.EventRepository;
import dartagnan.utils.Utils;
import dartagnan.wmm.relation.Relation;
import dartagnan.wmm.utils.Tuple;
import dartagnan.wmm.utils.TupleSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static dartagnan.utils.Utils.edge;
import static dartagnan.utils.Utils.intVar;

public class RelCo extends Relation {

    public RelCo(){
        term = "co";
        forceDoEncode = true;
    }

    @Override
    public TupleSet getMaxTupleSet(){
        if(maxTupleSet == null){
            maxTupleSet = new TupleSet();
            Collection<Event> eventsStore = program.getEventRepository().getEvents(EventRepository.STORE);
            Collection<Event> eventsInit = program.getEventRepository().getEvents(EventRepository.INIT);

            for(Event e1 : eventsInit){
                for(Event e2 : eventsStore){
                    if(MemEvent.canAddressTheSameLocation((MemEvent) e1, (MemEvent)e2)){
                        maxTupleSet.add(new Tuple(e1, e2));
                    }
                }
            }

            for(Event e1 : eventsStore){
                for(Event e2 : eventsStore){
                    if(e1.getEId() != e2.getEId() && MemEvent.canAddressTheSameLocation((MemEvent) e1, (MemEvent)e2)){
                        maxTupleSet.add(new Tuple(e1, e2));
                    }
                }
            }
        }
        return maxTupleSet;
    }

    @Override
    protected BoolExpr encodeApprox() {
        BoolExpr enc = ctx.mkTrue();
        EventRepository eventRepository = program.getEventRepository();
        ImmutableSetMultimap<Address, Location> addressLocationMap = getAddressLocationMap();
        Set<Event> writes = eventRepository.getEvents(EventRepository.STORE | EventRepository.INIT);

        for(Event e : eventRepository.getEvents(EventRepository.INIT)) {
            enc = ctx.mkAnd(enc, ctx.mkEq(intVar("co", e, ctx), ctx.mkInt(1)));
        }

        List<IntExpr> intVars = new ArrayList<>();
        for(Event w : eventRepository.getEvents(EventRepository.STORE)) {
            intVars.add(intVar("co", w, ctx));
            enc = ctx.mkAnd(enc, ctx.mkGt(Utils.intVar("co", w, ctx), ctx.mkInt(1)));
        }
        enc = ctx.mkAnd(enc, ctx.mkDistinct(intVars.toArray(new IntExpr[0])));

        for(Event w :  writes){
            MemEvent w1 = (MemEvent)w;
            BoolExpr lastCo = w1.executes(ctx);

            for(Tuple t : maxTupleSet.getByFirst(w1)){
                MemEvent w2 = (MemEvent)t.getSecond();
                BoolExpr relation = edge("co", w1, w2, ctx);
                lastCo = ctx.mkAnd(lastCo, ctx.mkNot(edge("co", w1, w2, ctx)));

                enc = ctx.mkAnd(enc, ctx.mkEq(relation, ctx.mkAnd(
                        ctx.mkAnd(ctx.mkAnd(w1.executes(ctx), w2.executes(ctx)), ctx.mkEq(w1.getMemAddressExpr(), w2.getMemAddressExpr())),
                        ctx.mkLt(Utils.intVar("co", w1, ctx), Utils.intVar("co", w2, ctx))
                )));
            }

            BoolExpr lastCoExpr = ctx.mkBoolConst("co_last(" + w1.repr() + ")");
            enc = ctx.mkAnd(enc, ctx.mkEq(lastCoExpr, lastCo));

            for(Address address : w1.getMaxAddressSet()){
                for(Location location : addressLocationMap.get(address)){
                    enc = ctx.mkAnd(enc, ctx.mkImplies(
                            ctx.mkAnd(lastCoExpr, ctx.mkEq(w1.getMemAddressExpr(), address.toZ3Int(ctx))),
                            ctx.mkEq(location.getLastValueExpr(ctx), w1.getMemValueExpr())
                    ));
                }
            }
        }
        return enc;
    }

    private ImmutableSetMultimap<Address, Location> getAddressLocationMap(){
        ImmutableSetMultimap.Builder<Address, Location> builder = new ImmutableSetMultimap.Builder<>();
        for(Location location : program.getLocations()){
            builder.put(location.getAddress(), location);
        }
        return builder.build();
    }
}
