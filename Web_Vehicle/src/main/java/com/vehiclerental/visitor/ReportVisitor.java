package com.vehiclerental.visitor;

import com.vehiclerental.common.entity.Rental;
import com.vehiclerental.common.entity.Vehicle;
import com.vehiclerental.common.entity.User;

public interface ReportVisitor {
    void visit(Rental rental);
    void visit(Vehicle vehicle);
    void visit(User user);
}