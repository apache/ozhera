package com.xiaomi.youpin.prometheus.agent.param.alert;


import com.xiaomi.youpin.prometheus.agent.param.BaseParam;
import lombok.Data;
import lombok.ToString;


import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@ToString(callSuper = true)
public class RuleSilenceParam extends BaseParam {

    private String id;
    private List<Matcher> matcher;
    private Long startsAt;
    private Long endsAt;
    private String comment;
    private String createdBy;

    public boolean argCheck() {
        //The comment cannot be empty.
        if (comment.equals("") || comment.isEmpty()) {
            return false;
        }
        // alertId and matcher cannot both be present or both be empty.
        //Check Matchers
        if (!ValidateMatchers(matcher)) {
            return false;
        }
        //Time synchronization
        if (startsAt == null || endsAt == null || startsAt < endsAt) {
            return false;
        }

        return true;
    }

    private boolean ValidateMatchers(List<Matcher> matchers) {
        AtomicBoolean valid = new AtomicBoolean(true);
        //The ame and value fields cannot be empty, isEqual and isRegex default to false if not passed.
        matchers.forEach(matcher -> {
            if (matcher.getName().isEmpty() || matcher.getValue().isEmpty() || (!matcher.isEqual() && matcher.isRegex())) {
                valid.set(false);
            }
        });
        return valid.get();
    }
}
