/* $Id: List.java,v 1.1 2005/06/29 15:14:40 codmon Exp $ */

import java.io.IOException;
import java.io.Serializable;

public final class List implements Serializable {

    public static final int PAYLOAD = 4*4;

    List next;

    int i;
    int i1;
    int i2;
    int i3;

    public List(int size) {
	if (size > 0) {
	    this.next = new List(size-1);
	}
    }
}





