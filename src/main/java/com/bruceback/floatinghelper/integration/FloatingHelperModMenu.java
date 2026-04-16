package com.bruceback.floatinghelper.integration;

import com.bruceback.floatinghelper.screen.FloatingHelperConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class FloatingHelperModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FloatingHelperConfigScreen::new;
    }
}
