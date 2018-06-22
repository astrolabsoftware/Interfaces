
package ca;

import com.sun.jna.Structure;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public interface Point {
    class P extends Structure {
        protected List getFieldOrder() {
            return Arrays.asList(new String[]{"x", "y", "z"});
        }

        public double x = 0, y = 0, z = 0;
    }
}



