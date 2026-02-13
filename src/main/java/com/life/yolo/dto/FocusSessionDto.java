package com.life.yolo.dto;

import com.life.yolo.entity.FocusSession;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FocusSessionDto extends FocusSession {
    private String goalTitle;
}
