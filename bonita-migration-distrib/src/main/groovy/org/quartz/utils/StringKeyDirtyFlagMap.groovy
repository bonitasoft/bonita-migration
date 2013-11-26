package org.quartz.utils;

public class StringKeyDirtyFlagMap extends DirtyFlagMap {

    static final long serialVersionUID = -9076749120524952280L;

    public StringKeyDirtyFlagMap() {
        super();
    }

    public StringKeyDirtyFlagMap(final int initialCapacity, final float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public StringKeyDirtyFlagMap(final int initialCapacity) {
        super(initialCapacity);
    }
}
