package org.quartz;

import java.io.Serializable;

import org.quartz.utils.StringKeyDirtyFlagMap;

public class JobDataMap extends StringKeyDirtyFlagMap implements Serializable {

    private static final long serialVersionUID = -6939901990106713909L;

    public JobDataMap() {
        super();
        // TODO Auto-generated constructor stub
    }

    public JobDataMap(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
        // TODO Auto-generated constructor stub
    }

    public JobDataMap(final int initialCapacity) {
        super(initialCapacity);
        // TODO Auto-generated constructor stub
    }

}
