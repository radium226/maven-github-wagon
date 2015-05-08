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
import com.google.common.base.Optional;

public final class Either<L, R> {

    private final Optional<L> left;
    private final Optional<R> right;

    private Either(Optional<L> left, Optional<R> right) {
        super();

        this.left = left;
        this.right = right;
    }

    public static <L, R> Either<L, R> left(L left) {
        return new Either(Optional.of(left), Optional.absent());
    }

    public boolean isLeft() {
        return left.isPresent();
    }

    public boolean isRight() {
        return right.isPresent();
    }

    public static <L, R> Either<L, R> right(R right) {
        return new Either(Optional.absent(), Optional.of(right));
    }

    public <M> M map(Function<L, M> leftFunction, Function<R, M> rightFunction) {
        M result = null;
        if (this.isLeft() && !this.isRight()) {
            result = leftFunction.apply(left.get());
        } else if (this.isRight() && !this.isLeft()) {
            result = rightFunction.apply(right.get());
        } else {
            throw new IllegalStateException("Either on of isRight() or isLeft() should be true");
        }
        return result;
    }

    public L getLeft() {
        return left.get();
    }

    public R getRight() {
        return right.get();
    }
    
    @Override
    public boolean equals(Object object) {
        boolean equality = false;
        if (object instanceof Either) {
            Either<?, ?> that = (Either<?, ?>) object;
            equality = Objects.equal(this.left, that.left) && Objects.equal(this.right, that.right);
        }
        return equality;
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(this.left, this.right);
    }
    
    @Override
    public String toString() {
        return MoreObjects.toStringHelper(Either.class)
                .add("left", this.left)
                .add("right", this.right)
            .toString();
    }

}
