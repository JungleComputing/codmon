/* $Id: TestObject.java,v 1.2 2007/05/24 16:27:41 ceriel Exp $ */

public interface TestObject extends java.io.Serializable {
    int object_size();

    int payload();

    int num_objs();

    String id();
}
