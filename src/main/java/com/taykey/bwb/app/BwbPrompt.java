package com.taykey.bwb.app;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class BwbPrompt implements PromptProvider{

    @Override
    public AttributedString getPrompt() {
        return new AttributedString("bwb:>",
                AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
    }

}
