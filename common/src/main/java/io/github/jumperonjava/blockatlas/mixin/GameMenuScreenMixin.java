package io.github.jumperonjava.blockatlas.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    @ModifyArg(method = "initWidgets",index = 1,at = @At(value = "INVOKE",target = "Lnet/minecraft/client/gui/widget/GridWidget$Adder;add(Lnet/minecraft/client/gui/widget/Widget;I)Lnet/minecraft/client/gui/widget/Widget;"))
    int takeonecolumn(int i){
        if(!this.client.isInSingleplayer())
        return 1;
        else return i;
    }
    @ModifyArg(method = "initWidgets",at = @At(value = "INVOKE",ordinal = 1,target = "Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;width(I)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"))
    int changeexitbuttonsize(int i) {
        if(!this.client.isInSingleplayer())
        return 98;
        else return i;
    }
    @Inject(method = "initWidgets", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/gui/widget/GridWidget;refreshPositions()V"))
    void addServersButton(CallbackInfo ci, @Local(ordinal = 0) GridWidget.Adder adder){
        if(!this.client.isInSingleplayer())
            adder.add(new ButtonWidget.Builder(Text.translatable("blockatlas.switch"),(b)-> {
                //new MultiplayerScreen(new TitleScreen());
                client.setScreen(new MultiplayerScreen(this));
                }).width(98).build());
    }
}
