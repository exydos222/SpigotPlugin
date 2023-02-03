package objects.bases;

import java.io.Serializable;
import java.util.UUID;

import enums.bases.BaseRank;

@SuppressWarnings("serial")
public class BaseMember implements Serializable {
	
	public BaseRank rank;
	public UUID uuid;
	
	public BaseMember(final BaseRank rank, final UUID uuid) {
		this.rank = rank;
		this.uuid = uuid;
	}
	
}
