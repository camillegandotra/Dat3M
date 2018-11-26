package dartagnan.program.event.rmw;

import dartagnan.expression.ExprInterface;
import dartagnan.program.memory.Location;
import dartagnan.program.event.Store;
import dartagnan.program.event.utils.RegReaderData;
import dartagnan.program.utils.EType;

public class RMWStore extends Store implements RegReaderData {

    protected RMWLoad loadEvent;

    public RMWStore(RMWLoad loadEvent, Location loc, ExprInterface value, String atomic) {
        super(loc, value, atomic);
        addFilters(EType.RMW);
        this.loadEvent = loadEvent;
    }

    public RMWLoad getLoadEvent(){
        return loadEvent;
    }

    @Override
    public RMWStore clone() {
        if(clone == null){
            clone = new RMWStore(loadEvent.clone(), loc.clone(), value.clone(), atomic);
            afterClone();
        }
        return (RMWStore)clone;
    }
}
