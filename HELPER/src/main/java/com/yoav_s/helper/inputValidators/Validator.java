package com.yoav_s.helper.inputValidators;

import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class Validator {

    private static Rules rules = new Rules();

    public static void add(View view, RuleOperation operation, String message){
        rules.add(new Rule(view, operation, message));
    }

    public static void add(Rule rule){
        rules.add(rule);
    }

    public static boolean requiredValidator(Rule rule) {
        boolean isvalid = true;

        if (rule.getView() instanceof EditText) {
            isvalid = ((EditText) rule.getView()).getText() != null && !((EditText) rule.getView()).getText().toString().trim().isEmpty();
        }

        if (rule.getView() instanceof Spinner) {
            if (((Spinner) rule.getView()).getSelectedItemPosition() == 0)
                isvalid = false;
        }

        if (rule.getView() instanceof CheckBox) {
            if (!((CheckBox) rule.getView()).isChecked())
                isvalid = false;
        }

        if (rule.getView() instanceof RadioGroup) {
            int count = (((RadioGroup) rule.getView()).getChildCount());
            int unchecked = 0;
            for (int i = 0; i < count; i++) {
                RadioButton rd = (RadioButton) ((RadioGroup) rule.getView()).getChildAt(i);
                if (!rd.isChecked())
                    unchecked++;
            }
            if (count == unchecked)
                isvalid = false;
        }

        return  isvalid;
    }

    public static boolean regularExpressionValidatpor(String whatToCheck, String regexPattern){
        try {
            return Pattern.compile(regexPattern)
                    .matcher(whatToCheck)
                    .matches();
        }
        catch (Exception e){
            int i = 0;
            return false;
        }
    }

    public static boolean validate(){

        Set<View> failedViews = new HashSet<>();

        for (Rule rule : rules) {
            View v = rule.getView();

            // if a prior rule already failed for this view -> skip
            if (failedViews.contains(v)) continue;

            if (rule.getView() instanceof EditText) {
                switch (rule.getOperation()) {
                    case REQUIRED: {
                        if (!Validator.requiredValidator(rule)) {
                            ((EditText) rule.getView()).setError(rule.getMessage());
                            rule.isValid = false;
                            failedViews.add(v); // block further rules for this view
                        } else {
                            ((EditText) rule.getView()).setError(null);
                            rule.isValid = true;
                        }
                        break;
                    }

                    case TEXT:
                    case NAME:{
                        if (!TextRule.validate((TextRule) rule)){
                            ((EditText) rule.getView()).setError(rule.getMessage());
                            rule.isValid = false;
                        } else {
                            ((EditText) rule.getView()).setError(null);
                            rule.isValid = true;
                        }

                        if (rule.getPerviousMessage() != null && !rule.getPerviousMessage().isEmpty())
                            rule.setMessage(rule.getPerviousMessage());

                        break;
                    }

                    case HEBREW_TEXT:{
                        if (!TextRuleHebrew.validate((TextRuleHebrew) rule)){
                            ((EditText) rule.getView()).setError(rule.getMessage());
                            rule.isValid = false;
                        } else {
                            ((EditText) rule.getView()).setError(null);
                            rule.isValid = true;
                        }

                        break;
                    }

                    case NUMBER:{
                        if (!NumberRule.validate((NumberRule) rule)){
                            ((EditText) rule.getView()).setError(rule.getMessage());
                            rule.isValid = false;
                        }
                        else {
                            ((EditText) rule.getView()).setError(null);
                            rule.isValid = true;
                        }

                        break;
                    }

                    case PASSWORD:{
                        if (!PasswordRule.validate((PasswordRule) rule)){
                            ((EditText) rule.getView()).setError(rule.getMessage());
                            rule.isValid = false;
                        } else {
                            ((EditText) rule.getView()).setError(null);
                            rule.isValid = true;
                        }

                        if (rule.getPerviousMessage() != null && !rule.getPerviousMessage().isEmpty())
                            rule.setMessage(rule.getPerviousMessage());

                        break;
                    }

                    case DATE:{
                        if (!DateRule.validate((DateRule) rule)){
                            ((EditText) rule.getView()).setError(rule.getMessage());
                            rule.isValid = false;
                        } else {
                            ((EditText) rule.getView()).setError(null);
                            rule.isValid = true;
                        }

                        break;
                    }

                    case COMPARE:{
                        if (!CompareRule.validate((CompareRule) rule)) {
                            ((EditText) rule.getView()).setError(rule.getMessage());
                            rule.isValid = false;
                        }
                        else {
                            ((EditText) rule.getView()).setError(null);
                            rule.isValid = true;
                        }

                        break;
                    }
                }
            }

            if (rule.getView() instanceof Spinner) {
                if (!Validator.requiredValidator(rule)) {
                    TextView errorText = (TextView) ((Spinner) rule.getView()).getSelectedView();
                    errorText.setTextColor(Color.RED);//just to highlight that this is an error
                    errorText.setText(errorText.getText());//changes the selected item text to this
                    errorText.setError(rule.getMessage());
                    rule.isValid = false;
                } else {
                    ((TextView) ((Spinner) rule.getView()).getSelectedView()).setError(null);
                    rule.isValid = true;
                }
            }

            if (rule.getView() instanceof CheckBox) {
                if (!Validator.requiredValidator(rule)) {
                    ((CheckBox) rule.getView()).setError(rule.getMessage());
                    rule.isValid = false;
                } else {
                    ((CheckBox) rule.getView()).setError(null);
                    rule.isValid = true;
                }
            }

            if (rule.getView() instanceof RadioGroup) {
                if (!Validator.requiredValidator(rule)) {
                    ((RadioButton) ((RadioGroup) rule.getView()).getChildAt(0)).setError(rule.getMessage());
                    rule.isValid = false;
                }
                else {
                    ((RadioButton)((RadioGroup)rule.getView()).getChildAt(0)).setError(null);
                    rule.isValid = true;
                }
            }

            if (rule.getView() instanceof android.widget.ImageView /*&& rule instanceof ImageRule*/) {
                ImageRule imgRule = (ImageRule) rule;
                boolean ok = imgRule.getIsValid();
                if (!ok) {
                    // mark failed (your existing failedViews logic)
                    failedViews.add(rule.getView());
                    rule.isValid = false;
                    ((ImageRule)rule).showError();
                } else {
                    // clear previous badge if any
                    rule.isValid = true;
                    ((ImageRule)rule).removeError();
                }
            }
        }

        boolean isValid = true;
        for (Rule rule : rules) {
            isValid = isValid && rule.isValid;
        }

        return  isValid;
    }

    public static void clear(){
        rules.clear();
    }
}

