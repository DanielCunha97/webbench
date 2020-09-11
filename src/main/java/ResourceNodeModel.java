import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;

public class ResourceNodeModel {

        public String firstRsrc;

        public ResourceNodeModel(){

        }

        @Override
        public int hashCode() {
                return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
                        // if deriving: appendSuper(super.hashCode()).
                                append(firstRsrc).
                                toHashCode();
        }

        @Override
        public boolean equals(Object obj) {
                if (!(obj instanceof ResourceNodeModel))
                        return false;
                if (obj == this)
                        return true;

                ResourceNodeModel rhs = (ResourceNodeModel) obj;
                return new EqualsBuilder().
                        // if deriving: appendSuper(super.equals(obj)).
                                append(firstRsrc, rhs.firstRsrc).
                                isEquals();
        }

}