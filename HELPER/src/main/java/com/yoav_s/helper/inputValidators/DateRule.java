package com.yoav_s.helper.inputValidators;

import android.view.View;
import android.widget.EditText;

import java.time.LocalDate;

import com.yoav_s.helper.DateUtil_OLD;

public class DateRule extends Rule{
    private static LocalDate lowBound;
    private static LocalDate upBound;

    public DateRule(View view, RuleOperation operation, String message, LocalDate lowBound, LocalDate upBound) {
        super(view, operation, message);

        DateRule.lowBound = lowBound;
        DateRule.upBound = upBound;
    }

    public LocalDate getLowBound(){
        return lowBound;
    }

    public LocalDate getUpBound(){
        return upBound;
    }

    public static boolean validate(DateRule rule){
        if (Validator.requiredValidator(rule)){
            LocalDate dateToCheck = DateUtil_OLD.stringToLocalDate(((EditText)rule.view).getText().toString());
            if (dateToCheck != null) {
                rule.isValid = DateUtil_OLD.inRange(dateToCheck, lowBound, upBound);
            }
            else {
                rule.isValid = false;
            }
        }
        else {
            rule.isValid = false;
        }

        return rule.isValid;
    }
}
