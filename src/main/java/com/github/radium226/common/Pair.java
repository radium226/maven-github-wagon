/**
 *    Copyright 2015 Radium226
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.radium226.common;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public final class Pair<F, S> {

    private final F first;
    private final S second;

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

    public <T> Pair<F, T> mapSecond(Function<S, T> function) {
        return Pair.of(this.first, function.apply(this.second));
    }

    public <T> Pair<T, S> mapFirst(Function<F, T> function) {
        return Pair.of(function.apply(this.first), this.second);
    }

}
