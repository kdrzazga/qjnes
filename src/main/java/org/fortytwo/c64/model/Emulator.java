package org.fortytwo.c64.model;

import org.fortytwo.c64.view.UI;
import org.fortytwo.c64.model.memory.Memory6502;
import org.fortytwo.c64.model.video.VICMemory;
import org.fortytwo.common.cpu.MOS6502Emulator;
import org.fortytwo.common.cpu.MOS6502InstructionSet;
import org.fortytwo.c64.model.io.Joystick;
import org.fortytwo.c64.model.io.Keyboard;
import org.fortytwo.common.memory.ROM;
import org.fortytwo.common.memory.StandardFactory;
import org.fortytwo.common.util.CRTFile;
import org.fortytwo.c64.model.video.VICII;

import java.io.File;
import java.io.IOException;

public class Emulator {

    protected static UI ui;

    public static MOS6502Emulator createMos6502Emulator(String[] params) throws IOException {
        final var cpu = new MOS6502Emulator(new MOS6502InstructionSet());

        var keyboard = new Keyboard(cpu);
        var joystick = new Joystick();
        var joystick2 = new Joystick();
        //_1541 diskDrive = new _1541();

        var cia1 = setupCia1(keyboard, joystick);
        //cia2.setIODevice1(joystick2);
        //cia2.setIODevice(diskDrive);

        int breakpoint = (params.length > 0) ? Integer.parseInt(params[0], 16) : 0;

        var cartridgeFile = (params.length > 1) ? new File(params[1]) : null;

        ROM cartridgeROM = null;
        int romStart = 0;
        CRTFile crtFile = null;

        if (cartridgeFile != null) {
            System.out.println("reading cartridge from: " + cartridgeFile.getName());
            crtFile = new CRTFile(cartridgeFile);
            byte[] romData = crtFile.getCHIPData().getROMData();
            cartridgeROM = new ROM(crtFile.getName(), romData);
            romStart = crtFile.getCHIPData().getStartAddress();
            //cartridgeROM = new ROM("cartridge", cartridgeFile);

        }

        ui = new UI(cpu, keyboard, joystick);
        ui.invoke();

        var vic = createVic(ui);

        final Memory6502 memory6502 = createMemory(cia1, cartridgeROM, crtFile, vic);

        ui.initUnloadCartridgeMenuItem(memory6502);
        //	memory6502.enableLogging();
        setupCpu(cpu, cia1, breakpoint, vic, memory6502);
        setupVic(vic, memory6502);

        return cpu;
    }

    public static void enableSilentRun() {
        ui.hide();
    }

    static void setupCpu(MOS6502Emulator cpu, CIA cia1, int breakpoint, VICII vic, Memory6502 memory6502) {
        cpu.registerCycleObserver(cia1);
        cpu.registerCycleObserver(memory6502.getCia2());
        cpu.registerCycleObserver(vic);

        cpu.setMemory(memory6502);
        cpu.setBreak(breakpoint);
    }

    static void setupVic(VICII vic, Memory6502 memory6502) {
        /**
         * Now create the view that the VIC sees
         */
        var memoryVIC = new VICMemory(memory6502.getRam(), memory6502.getCia2(), memory6502.getCharROM(), memory6502.getColorRAM());
        vic.setMemory(memoryVIC);
    }

    static Memory6502 createMemory(CIA cia1, ROM cartridgeROM, CRTFile crtFile, VICII vic) throws IOException {
        final var memory6502 = new StandardFactory().createStandardMemory6502(vic, cia1, cartridgeROM);
        if (crtFile != null) {
            memory6502.setGameStatus(crtFile.getGameStatus());
            memory6502.setExromStatus(crtFile.getExromStatus());
        }
        return memory6502;
    }

    static VICII createVic(UI ui) {
        var vic = new VICII();
        var videoScreen = ui.getVideoScreen();
        vic.setScreen(videoScreen);
        return vic;
    }

    static CIA setupCia1(Keyboard keyboard, Joystick joystick) {
        var cia1 = new CIA("CIA1");

        cia1.setKeyboard(keyboard);
        cia1.setJoystick1(joystick);

        //cia1.setIODevice1(keyboard);
        //cia1.setIODevice1(joystick2);
        //            cia1.setIODevice2(joystick);
        return cia1;
    }

}
