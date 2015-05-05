package com.github.radium226.common;

import com.google.common.base.Function;
import com.google.common.base.Optional;

public class Either<L, R> {

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
    
}
