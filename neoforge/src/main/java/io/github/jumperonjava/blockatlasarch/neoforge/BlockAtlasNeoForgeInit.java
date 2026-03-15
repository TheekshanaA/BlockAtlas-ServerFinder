package io.github.jumperonjava.blockatlasarch.neoforge;

import io.github.jumperonjava.blockatlas.BlockAtlasInit;
import net.neoforged.fml.common.Mod;

@Mod("blockatlas")
public class BlockAtlasNeoForgeInit {
    public BlockAtlasNeoForgeInit(){
        new BlockAtlasInit();
    }
}
