/* $Id: DITree.java,v 1.2 2007/05/24 16:27:41 ceriel Exp $ */

import ibis.util.TypedProperties;

public final class DITree implements TestObject {

    /**
     * 
     */
    private static final long serialVersionUID = 9171355015211077799L;

    static final int OBJECT_SIZE = 4 * 4 + 2 * 4;

    static final int KARMI_SIZE = 4 * 4;

    static final TypedProperties tp = new TypedProperties(System.getProperties());
    static final int LEN = tp.getIntProperty("len", 1023);

    DITree left;

    DITree right;

    int size;

    int i1;

    int i2;

    int i3;

    public DITree() {
        this(LEN);
    }

    private DITree(int size) {
        int leftSize = size / 2;
        if (leftSize > 0) {
            this.left = new DITree(leftSize);
        }
        if (size - leftSize - 1 > 0) {
            this.right = new DITree(size - leftSize - 1);
        }
        this.size = size;
    }

    public int object_size() {
        return size * OBJECT_SIZE;
    }

    public int payload() {
        return size * KARMI_SIZE;
    }

    public String id() {
        return "Tree of " + size + " DITree objects";
    }

    public int num_objs() {
        return size;
    }
}
