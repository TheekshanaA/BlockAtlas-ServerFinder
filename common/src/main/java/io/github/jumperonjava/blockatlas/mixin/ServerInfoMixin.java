package io.github.jumperonjava.blockatlas.mixin;

import io.github.jumperonjava.blockatlas.util.ServerInfoExt;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerInfo.class)
public class ServerInfoMixin implements ServerInfoExt {
    @Shadow public String address;
    private static String VOTELINK_TAG = "voteLink";
    private static String POSTREQUESTCALL_TAG = "postreqcall";
    public String votelink=null;
    public String postRequestCall=null;

    @Inject(method = "toNbt",at = @At(value = "INVOKE",target = "Lnet/minecraft/nbt/NbtCompound;putString(Ljava/lang/String;Ljava/lang/String;)V",ordinal = 0),locals = LocalCapture.CAPTURE_FAILHARD)
    void toNbtAddVoteUrl(CallbackInfoReturnable<NbtCompound> cir, NbtCompound nbtCompound){
        if(votelink!=null)
            nbtCompound.putString(VOTELINK_TAG,votelink);
        if(postRequestCall!=null)
            nbtCompound.putString(POSTREQUESTCALL_TAG,postRequestCall);
    }

    @Inject(method = "fromNbt",at = @At(value = "INVOKE",target = "Lnet/minecraft/nbt/NbtCompound;contains(Ljava/lang/String;I)Z",ordinal = 0,shift = At.Shift.AFTER),locals = LocalCapture.CAPTURE_FAILHARD)
    private static void fromNbtParseVoteUrl(NbtCompound root, CallbackInfoReturnable<ServerInfo> cir, ServerInfo serverInfo){
        if(root.contains(VOTELINK_TAG)){
            ((ServerInfoMixin)(Object)serverInfo).votelink=root.getString(VOTELINK_TAG);
        }
        if(root.contains(POSTREQUESTCALL_TAG)){
            ((ServerInfoMixin)(Object)serverInfo).postRequestCall=root.getString(POSTREQUESTCALL_TAG);
        }
    }
    @Inject(method = "copyFrom",at = @At("HEAD"))
    private void copyVoteUrl(ServerInfo serverInfo, CallbackInfo ci){
        if(serverInfo.address.equals(this.address))
        {
            this.votelink = ((ServerInfoMixin)(Object)serverInfo).votelink;
            this.postRequestCall = ((ServerInfoMixin)(Object)serverInfo).postRequestCall;
        }
    }
    public void setVoteLink(String s) {
        this.votelink = s;
    }

    public String getVoteLink() {
        return votelink;
    }
    public void setPostReq(String s) {
        this.postRequestCall = s;
    }
    public String getPostReq() {
        return postRequestCall;
    }
}
