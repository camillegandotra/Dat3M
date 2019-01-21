package dartagnan.program.event;

import dartagnan.program.utils.EType;

public class Fence extends Event {

	protected String name;

	public Fence(String name){
        this.name = name;
        this.addFilters(EType.ANY, EType.FENCE, name);
	}

	public String getName(){
		return name;
	}

	@Override
	public String toString() {
		if(atomic == null){
			return nTimesCondLevel() + getName();
		}
		return nTimesCondLevel() + getName();
	}

	@Override
	public Fence clone() {
		if(clone == null){
			clone = new Fence(name);
			afterClone();
		}
		return (Fence)clone;
	}

	@Override
	public String label(){
		return getName();
	}
}
