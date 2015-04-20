package com.github.radium.common;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Pair<F, S> {

    private F first; 
    private S second;
    
    private Pair(F first, S second) {
        super();
        
        this.first = first;
        this.second = second;
    }
    
    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair(first, second);
    }
    
    public F getFirst() {
        return this.first;
    }
    
    public S getSecond() {
        return this.second;
    }
    
    @Override
    public boolean equals(Object object) {
        boolean equal = false;
        if (object instanceof Pair) {
            Pair that = (Pair) object;
            equal = Objects.equal(this.first, that.first) && Objects.equal(this.second, that.second);
        }
        return equal;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(this.first, this.second);
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Pair.class)
                .add("first", this.first)
                .add("second", this.second)
            .toString();
    }
    
}
