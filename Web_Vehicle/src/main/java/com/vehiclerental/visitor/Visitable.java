package com.vehiclerental.visitor;

public interface Visitable {
    void accept(ReportVisitor visitor);
}