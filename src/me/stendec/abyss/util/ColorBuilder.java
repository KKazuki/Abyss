package me.stendec.abyss.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public final class ColorBuilder implements Appendable, CharSequence, Serializable {

    private final StringBuilder sb;

    private static final Pattern fpattern = Pattern.compile("%(?:\\d+\\$)?[-#+ 0,(]*\\d*(?:\\.\\d+)?(?:[bBhHsScCdoxXeEfgGaA%n]|[tT][ABCDFHILMNQRSTYZabcdehjklmprsyz])");

    public ColorBuilder() {
        sb = new StringBuilder();
    }

    public ColorBuilder(final int capacity) {
        sb = new StringBuilder(capacity);
    }

    public ColorBuilder(final String str) {
        sb = new StringBuilder(str);
    }

    public ColorBuilder(final CharSequence seq) {
        sb = new StringBuilder(seq);
    }

    public ColorBuilder(final String... strings) {
        sb = new StringBuilder();
        for(final String string: strings)
            sb.append(string);
    }

    ///////////////////////////////////////////////////////////////////////////
    // My Stuff
    ///////////////////////////////////////////////////////////////////////////

    public final ColorBuilder format(final String format, final Object... args) {
        sb.append(String.format(format, args));
        return this;
    }

    public final ColorBuilder lf() {
        sb.append('\n');
        return this;
    }

    public final ColorBuilder send(final CommandSender sender) {
        sender.sendMessage(sb.toString());
        return this;
    }

    public final ColorBuilder send(final CommandSender... senders) {
        final String out = sb.toString();
        for(final CommandSender sender: senders)
            sender.sendMessage(out);

        return this;
    }

    public final ColorBuilder send(final Logger logger) {
        return send(logger, Level.INFO);
    }

    public final ColorBuilder send(final Logger logger, final Level level) {
        for(final String string: ChatColor.stripColor(sb.toString()).split("\n"))
            logger.log(level, string);
        return this;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Color Magic
    ///////////////////////////////////////////////////////////////////////////

    public final ColorBuilder aqua() { sb.append(ChatColor.AQUA); return this; }
    public final ColorBuilder black() { sb.append(ChatColor.BLACK); return this; }
    public final ColorBuilder blue() { sb.append(ChatColor.BLUE); return this; }
    public final ColorBuilder darkaqua() { sb.append(ChatColor.DARK_AQUA); return this; }
    public final ColorBuilder darkblue() { sb.append(ChatColor.DARK_BLUE); return this; }
    public final ColorBuilder darkgray() { sb.append(ChatColor.DARK_GRAY); return this; }
    public final ColorBuilder darkgreen() { sb.append(ChatColor.DARK_GREEN); return this; }
    public final ColorBuilder darkpurple() { sb.append(ChatColor.DARK_PURPLE); return this; }
    public final ColorBuilder darkred() { sb.append(ChatColor.DARK_RED); return this; }
    public final ColorBuilder gold() { sb.append(ChatColor.GOLD); return this; }
    public final ColorBuilder gray() { sb.append(ChatColor.GRAY); return this; }
    public final ColorBuilder green() { sb.append(ChatColor.GREEN); return this; }
    public final ColorBuilder lightpurple() { sb.append(ChatColor.LIGHT_PURPLE); return this; }
    public final ColorBuilder red() { sb.append(ChatColor.RED); return this; }
    public final ColorBuilder white() { sb.append(ChatColor.WHITE); return this; }
    public final ColorBuilder yellow() { sb.append(ChatColor.YELLOW); return this; }
    public final ColorBuilder bold() { sb.append(ChatColor.BOLD); return this; }
    public final ColorBuilder italic() { sb.append(ChatColor.ITALIC); return this; }
    public final ColorBuilder strike() { sb.append(ChatColor.STRIKETHROUGH); return this; }
    public final ColorBuilder under() { sb.append(ChatColor.UNDERLINE); return this; }
    public final ColorBuilder reset() { sb.append(ChatColor.RESET); return this; }
    public final ColorBuilder magic() { sb.append(ChatColor.MAGIC); return this; }

    public final ColorBuilder aqua(final String format, final Object... args) {
        sb.append(ChatColor.AQUA).append(String.format(format, args)); return this; }

    public final ColorBuilder black(final String format, final Object... args) {
        sb.append(ChatColor.BLACK).append(String.format(format, args)); return this; }

    public final ColorBuilder blue(final String format, final Object... args) {
        sb.append(ChatColor.BLUE).append(String.format(format, args)); return this; }

    public final ColorBuilder darkaqua(final String format, final Object... args) {
        sb.append(ChatColor.DARK_AQUA).append(String.format(format, args)); return this; }

    public final ColorBuilder darkblue(final String format, final Object... args) {
        sb.append(ChatColor.DARK_BLUE).append(String.format(format, args)); return this; }

    public final ColorBuilder darkgray(final String format, final Object... args) {
        sb.append(ChatColor.DARK_GRAY).append(String.format(format, args)); return this; }

    public final ColorBuilder darkgreen(final String format, final Object... args) {
        sb.append(ChatColor.DARK_GREEN).append(String.format(format, args)); return this; }

    public final ColorBuilder darkpurple(final String format, final Object... args) {
        sb.append(ChatColor.DARK_PURPLE).append(String.format(format, args)); return this; }

    public final ColorBuilder darkred(final String format, final Object... args) {
        sb.append(ChatColor.DARK_RED).append(String.format(format, args)); return this; }

    public final ColorBuilder gold(final String format, final Object... args) {
        sb.append(ChatColor.GOLD).append(String.format(format, args)); return this; }

    public final ColorBuilder gray(final String format, final Object... args) {
        sb.append(ChatColor.GRAY).append(String.format(format, args)); return this; }

    public final ColorBuilder green(final String format, final Object... args) {
        sb.append(ChatColor.GREEN).append(String.format(format, args)); return this; }

    public final ColorBuilder lightpurple(final String format, final Object... args) {
        sb.append(ChatColor.LIGHT_PURPLE).append(String.format(format, args)); return this; }

    public final ColorBuilder red(final String format, final Object... args) {
        sb.append(ChatColor.RED).append(String.format(format, args)); return this; }

    public final ColorBuilder white(final String format, final Object... args) {
        sb.append(ChatColor.WHITE).append(String.format(format, args)); return this; }

    public final ColorBuilder yellow(final String format, final Object... args) {
        sb.append(ChatColor.YELLOW).append(String.format(format, args)); return this; }

    public final ColorBuilder bold(final String format, final Object... args) {
        sb.append(ChatColor.BOLD).append(String.format(format, args)); return this; }

    public final ColorBuilder italic(final String format, final Object... args) {
        sb.append(ChatColor.ITALIC).append(String.format(format, args)); return this; }

    public final ColorBuilder strike(final String format, final Object... args) {
        sb.append(ChatColor.STRIKETHROUGH).append(String.format(format, args)); return this; }

    public final ColorBuilder under(final String format, final Object... args) {
        sb.append(ChatColor.UNDERLINE).append(String.format(format, args)); return this; }

    public final ColorBuilder reset(final String format, final Object... args) {
        sb.append(ChatColor.RESET).append(String.format(format, args)); return this; }

    public final ColorBuilder magic(final String format, final Object... args) {
        sb.append(ChatColor.MAGIC).append(String.format(format, args)); return this; }

    public final ColorBuilder aqua(final ChatColor color, final String format, final Object... args) {
        return aqua(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.AQUA), args); }

    public final ColorBuilder black(final ChatColor color, final String format, final Object... args) {
        return black(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.BLACK), args); }

    public final ColorBuilder blue(final ChatColor color, final String format, final Object... args) {
        return blue(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.BLUE), args); }

    public final ColorBuilder darkaqua(final ChatColor color, final String format, final Object... args) {
        return darkaqua(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.DARK_AQUA), args); }

    public final ColorBuilder darkblue(final ChatColor color, final String format, final Object... args) {
        return darkblue(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.DARK_BLUE), args); }

    public final ColorBuilder darkgray(final ChatColor color, final String format, final Object... args) {
        return darkgray(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.DARK_GRAY), args); }

    public final ColorBuilder darkgreen(final ChatColor color, final String format, final Object... args) {
        return darkgreen(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.DARK_GREEN), args); }

    public final ColorBuilder darkpurple(final ChatColor color, final String format, final Object... args) {
        return darkpurple(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.DARK_PURPLE), args); }

    public final ColorBuilder darkred(final ChatColor color, final String format, final Object... args) {
        return darkred(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.DARK_RED), args); }

    public final ColorBuilder gold(final ChatColor color, final String format, final Object... args) {
        return gold(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.GOLD), args); }

    public final ColorBuilder gray(final ChatColor color, final String format, final Object... args) {
        return gray(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.GRAY), args); }

    public final ColorBuilder green(final ChatColor color, final String format, final Object... args) {
        return green(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.GREEN), args); }

    public final ColorBuilder lightpurple(final ChatColor color, final String format, final Object... args) {
        return lightpurple(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.LIGHT_PURPLE), args); }

    public final ColorBuilder red(final ChatColor color, final String format, final Object... args) {
        return red(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.RED), args); }

    public final ColorBuilder white(final ChatColor color, final String format, final Object... args) {
        return white(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.WHITE), args); }

    public final ColorBuilder yellow(final ChatColor color, final String format, final Object... args) {
        return yellow(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.YELLOW), args); }

    public final ColorBuilder bold(final ChatColor color, final String format, final Object... args) {
        return bold(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.BOLD), args); }

    public final ColorBuilder italic(final ChatColor color, final String format, final Object... args) {
        return italic(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.ITALIC), args); }

    public final ColorBuilder strike(final ChatColor color, final String format, final Object... args) {
        return strike(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.STRIKETHROUGH), args); }

    public final ColorBuilder under(final ChatColor color, final String format, final Object... args) {
        return under(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.UNDERLINE), args); }

    public final ColorBuilder reset(final ChatColor color, final String format, final Object... args) {
        return reset(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.RESET), args); }

    public final ColorBuilder magic(final ChatColor color, final String format, final Object... args) {
        return magic(fpattern.matcher(format).replaceAll(color + "$0" + ChatColor.MAGIC), args); }

    public final ColorBuilder aqua(final boolean b) {
        sb.append(ChatColor.AQUA).append(b); return this; }

    public final ColorBuilder black(final boolean b) {
        sb.append(ChatColor.BLACK).append(b); return this; }

    public final ColorBuilder blue(final boolean b) {
        sb.append(ChatColor.BLUE).append(b); return this; }

    public final ColorBuilder darkaqua(final boolean b) {
        sb.append(ChatColor.DARK_AQUA).append(b); return this; }

    public final ColorBuilder darkblue(final boolean b) {
        sb.append(ChatColor.DARK_BLUE).append(b); return this; }

    public final ColorBuilder darkgray(final boolean b) {
        sb.append(ChatColor.DARK_GRAY).append(b); return this; }

    public final ColorBuilder darkgreen(final boolean b) {
        sb.append(ChatColor.DARK_GREEN).append(b); return this; }

    public final ColorBuilder darkpurple(final boolean b) {
        sb.append(ChatColor.DARK_PURPLE).append(b); return this; }

    public final ColorBuilder darkred(final boolean b) {
        sb.append(ChatColor.DARK_RED).append(b); return this; }

    public final ColorBuilder gold(final boolean b) {
        sb.append(ChatColor.GOLD).append(b); return this; }

    public final ColorBuilder gray(final boolean b) {
        sb.append(ChatColor.GRAY).append(b); return this; }

    public final ColorBuilder green(final boolean b) {
        sb.append(ChatColor.GREEN).append(b); return this; }

    public final ColorBuilder lightpurple(final boolean b) {
        sb.append(ChatColor.LIGHT_PURPLE).append(b); return this; }

    public final ColorBuilder red(final boolean b) {
        sb.append(ChatColor.RED).append(b); return this; }

    public final ColorBuilder white(final boolean b) {
        sb.append(ChatColor.WHITE).append(b); return this; }

    public final ColorBuilder yellow(final boolean b) {
        sb.append(ChatColor.YELLOW).append(b); return this; }

    public final ColorBuilder bold(final boolean b) {
        sb.append(ChatColor.BOLD).append(b); return this; }

    public final ColorBuilder italic(final boolean b) {
        sb.append(ChatColor.ITALIC).append(b); return this; }

    public final ColorBuilder strike(final boolean b) {
        sb.append(ChatColor.STRIKETHROUGH).append(b); return this; }

    public final ColorBuilder under(final boolean b) {
        sb.append(ChatColor.UNDERLINE).append(b); return this; }

    public final ColorBuilder reset(final boolean b) {
        sb.append(ChatColor.RESET).append(b); return this; }

    public final ColorBuilder magic(final boolean b) {
        sb.append(ChatColor.MAGIC).append(b); return this; }


    public final ColorBuilder aqua(final char c) {
        sb.append(ChatColor.AQUA).append(c); return this; }

    public final ColorBuilder black(final char c) {
        sb.append(ChatColor.BLACK).append(c); return this; }

    public final ColorBuilder blue(final char c) {
        sb.append(ChatColor.BLUE).append(c); return this; }

    public final ColorBuilder darkaqua(final char c) {
        sb.append(ChatColor.DARK_AQUA).append(c); return this; }

    public final ColorBuilder darkblue(final char c) {
        sb.append(ChatColor.DARK_BLUE).append(c); return this; }

    public final ColorBuilder darkgray(final char c) {
        sb.append(ChatColor.DARK_GRAY).append(c); return this; }

    public final ColorBuilder darkgreen(final char c) {
        sb.append(ChatColor.DARK_GREEN).append(c); return this; }

    public final ColorBuilder darkpurple(final char c) {
        sb.append(ChatColor.DARK_PURPLE).append(c); return this; }

    public final ColorBuilder darkred(final char c) {
        sb.append(ChatColor.DARK_RED).append(c); return this; }

    public final ColorBuilder gold(final char c) {
        sb.append(ChatColor.GOLD).append(c); return this; }

    public final ColorBuilder gray(final char c) {
        sb.append(ChatColor.GRAY).append(c); return this; }

    public final ColorBuilder green(final char c) {
        sb.append(ChatColor.GREEN).append(c); return this; }

    public final ColorBuilder lightpurple(final char c) {
        sb.append(ChatColor.LIGHT_PURPLE).append(c); return this; }

    public final ColorBuilder red(final char c) {
        sb.append(ChatColor.RED).append(c); return this; }

    public final ColorBuilder white(final char c) {
        sb.append(ChatColor.WHITE).append(c); return this; }

    public final ColorBuilder yellow(final char c) {
        sb.append(ChatColor.YELLOW).append(c); return this; }

    public final ColorBuilder bold(final char c) {
        sb.append(ChatColor.BOLD).append(c); return this; }

    public final ColorBuilder italic(final char c) {
        sb.append(ChatColor.ITALIC).append(c); return this; }

    public final ColorBuilder strike(final char c) {
        sb.append(ChatColor.STRIKETHROUGH).append(c); return this; }

    public final ColorBuilder under(final char c) {
        sb.append(ChatColor.UNDERLINE).append(c); return this; }

    public final ColorBuilder reset(final char c) {
        sb.append(ChatColor.RESET).append(c); return this; }

    public final ColorBuilder magic(final char c) {
        sb.append(ChatColor.MAGIC).append(c); return this; }


    public final ColorBuilder aqua(final char[] str) {
        sb.append(ChatColor.AQUA).append(str); return this; }

    public final ColorBuilder black(final char[] str) {
        sb.append(ChatColor.BLACK).append(str); return this; }

    public final ColorBuilder blue(final char[] str) {
        sb.append(ChatColor.BLUE).append(str); return this; }

    public final ColorBuilder darkaqua(final char[] str) {
        sb.append(ChatColor.DARK_AQUA).append(str); return this; }

    public final ColorBuilder darkblue(final char[] str) {
        sb.append(ChatColor.DARK_BLUE).append(str); return this; }

    public final ColorBuilder darkgray(final char[] str) {
        sb.append(ChatColor.DARK_GRAY).append(str); return this; }

    public final ColorBuilder darkgreen(final char[] str) {
        sb.append(ChatColor.DARK_GREEN).append(str); return this; }

    public final ColorBuilder darkpurple(final char[] str) {
        sb.append(ChatColor.DARK_PURPLE).append(str); return this; }

    public final ColorBuilder darkred(final char[] str) {
        sb.append(ChatColor.DARK_RED).append(str); return this; }

    public final ColorBuilder gold(final char[] str) {
        sb.append(ChatColor.GOLD).append(str); return this; }

    public final ColorBuilder gray(final char[] str) {
        sb.append(ChatColor.GRAY).append(str); return this; }

    public final ColorBuilder green(final char[] str) {
        sb.append(ChatColor.GREEN).append(str); return this; }

    public final ColorBuilder lightpurple(final char[] str) {
        sb.append(ChatColor.LIGHT_PURPLE).append(str); return this; }

    public final ColorBuilder red(final char[] str) {
        sb.append(ChatColor.RED).append(str); return this; }

    public final ColorBuilder white(final char[] str) {
        sb.append(ChatColor.WHITE).append(str); return this; }

    public final ColorBuilder yellow(final char[] str) {
        sb.append(ChatColor.YELLOW).append(str); return this; }

    public final ColorBuilder bold(final char[] str) {
        sb.append(ChatColor.BOLD).append(str); return this; }

    public final ColorBuilder italic(final char[] str) {
        sb.append(ChatColor.ITALIC).append(str); return this; }

    public final ColorBuilder strike(final char[] str) {
        sb.append(ChatColor.STRIKETHROUGH).append(str); return this; }

    public final ColorBuilder under(final char[] str) {
        sb.append(ChatColor.UNDERLINE).append(str); return this; }

    public final ColorBuilder reset(final char[] str) {
        sb.append(ChatColor.RESET).append(str); return this; }

    public final ColorBuilder magic(final char[] str) {
        sb.append(ChatColor.MAGIC).append(str); return this; }


    public final ColorBuilder aqua(final CharSequence s) {
        sb.append(ChatColor.AQUA).append(s); return this; }

    public final ColorBuilder black(final CharSequence s) {
        sb.append(ChatColor.BLACK).append(s); return this; }

    public final ColorBuilder blue(final CharSequence s) {
        sb.append(ChatColor.BLUE).append(s); return this; }

    public final ColorBuilder darkaqua(final CharSequence s) {
        sb.append(ChatColor.DARK_AQUA).append(s); return this; }

    public final ColorBuilder darkblue(final CharSequence s) {
        sb.append(ChatColor.DARK_BLUE).append(s); return this; }

    public final ColorBuilder darkgray(final CharSequence s) {
        sb.append(ChatColor.DARK_GRAY).append(s); return this; }

    public final ColorBuilder darkgreen(final CharSequence s) {
        sb.append(ChatColor.DARK_GREEN).append(s); return this; }

    public final ColorBuilder darkpurple(final CharSequence s) {
        sb.append(ChatColor.DARK_PURPLE).append(s); return this; }

    public final ColorBuilder darkred(final CharSequence s) {
        sb.append(ChatColor.DARK_RED).append(s); return this; }

    public final ColorBuilder gold(final CharSequence s) {
        sb.append(ChatColor.GOLD).append(s); return this; }

    public final ColorBuilder gray(final CharSequence s) {
        sb.append(ChatColor.GRAY).append(s); return this; }

    public final ColorBuilder green(final CharSequence s) {
        sb.append(ChatColor.GREEN).append(s); return this; }

    public final ColorBuilder lightpurple(final CharSequence s) {
        sb.append(ChatColor.LIGHT_PURPLE).append(s); return this; }

    public final ColorBuilder red(final CharSequence s) {
        sb.append(ChatColor.RED).append(s); return this; }

    public final ColorBuilder white(final CharSequence s) {
        sb.append(ChatColor.WHITE).append(s); return this; }

    public final ColorBuilder yellow(final CharSequence s) {
        sb.append(ChatColor.YELLOW).append(s); return this; }

    public final ColorBuilder bold(final CharSequence s) {
        sb.append(ChatColor.BOLD).append(s); return this; }

    public final ColorBuilder italic(final CharSequence s) {
        sb.append(ChatColor.ITALIC).append(s); return this; }

    public final ColorBuilder strike(final CharSequence s) {
        sb.append(ChatColor.STRIKETHROUGH).append(s); return this; }

    public final ColorBuilder under(final CharSequence s) {
        sb.append(ChatColor.UNDERLINE).append(s); return this; }

    public final ColorBuilder reset(final CharSequence s) {
        sb.append(ChatColor.RESET).append(s); return this; }

    public final ColorBuilder magic(final CharSequence s) {
        sb.append(ChatColor.MAGIC).append(s); return this; }


    public final ColorBuilder aqua(final double d) {
        sb.append(ChatColor.AQUA).append(d); return this; }

    public final ColorBuilder black(final double d) {
        sb.append(ChatColor.BLACK).append(d); return this; }

    public final ColorBuilder blue(final double d) {
        sb.append(ChatColor.BLUE).append(d); return this; }

    public final ColorBuilder darkaqua(final double d) {
        sb.append(ChatColor.DARK_AQUA).append(d); return this; }

    public final ColorBuilder darkblue(final double d) {
        sb.append(ChatColor.DARK_BLUE).append(d); return this; }

    public final ColorBuilder darkgray(final double d) {
        sb.append(ChatColor.DARK_GRAY).append(d); return this; }

    public final ColorBuilder darkgreen(final double d) {
        sb.append(ChatColor.DARK_GREEN).append(d); return this; }

    public final ColorBuilder darkpurple(final double d) {
        sb.append(ChatColor.DARK_PURPLE).append(d); return this; }

    public final ColorBuilder darkred(final double d) {
        sb.append(ChatColor.DARK_RED).append(d); return this; }

    public final ColorBuilder gold(final double d) {
        sb.append(ChatColor.GOLD).append(d); return this; }

    public final ColorBuilder gray(final double d) {
        sb.append(ChatColor.GRAY).append(d); return this; }

    public final ColorBuilder green(final double d) {
        sb.append(ChatColor.GREEN).append(d); return this; }

    public final ColorBuilder lightpurple(final double d) {
        sb.append(ChatColor.LIGHT_PURPLE).append(d); return this; }

    public final ColorBuilder red(final double d) {
        sb.append(ChatColor.RED).append(d); return this; }

    public final ColorBuilder white(final double d) {
        sb.append(ChatColor.WHITE).append(d); return this; }

    public final ColorBuilder yellow(final double d) {
        sb.append(ChatColor.YELLOW).append(d); return this; }

    public final ColorBuilder bold(final double d) {
        sb.append(ChatColor.BOLD).append(d); return this; }

    public final ColorBuilder italic(final double d) {
        sb.append(ChatColor.ITALIC).append(d); return this; }

    public final ColorBuilder strike(final double d) {
        sb.append(ChatColor.STRIKETHROUGH).append(d); return this; }

    public final ColorBuilder under(final double d) {
        sb.append(ChatColor.UNDERLINE).append(d); return this; }

    public final ColorBuilder reset(final double d) {
        sb.append(ChatColor.RESET).append(d); return this; }

    public final ColorBuilder magic(final double d) {
        sb.append(ChatColor.MAGIC).append(d); return this; }


    public final ColorBuilder aqua(final float f) {
        sb.append(ChatColor.AQUA).append(f); return this; }

    public final ColorBuilder black(final float f) {
        sb.append(ChatColor.BLACK).append(f); return this; }

    public final ColorBuilder blue(final float f) {
        sb.append(ChatColor.BLUE).append(f); return this; }

    public final ColorBuilder darkaqua(final float f) {
        sb.append(ChatColor.DARK_AQUA).append(f); return this; }

    public final ColorBuilder darkblue(final float f) {
        sb.append(ChatColor.DARK_BLUE).append(f); return this; }

    public final ColorBuilder darkgray(final float f) {
        sb.append(ChatColor.DARK_GRAY).append(f); return this; }

    public final ColorBuilder darkgreen(final float f) {
        sb.append(ChatColor.DARK_GREEN).append(f); return this; }

    public final ColorBuilder darkpurple(final float f) {
        sb.append(ChatColor.DARK_PURPLE).append(f); return this; }

    public final ColorBuilder darkred(final float f) {
        sb.append(ChatColor.DARK_RED).append(f); return this; }

    public final ColorBuilder gold(final float f) {
        sb.append(ChatColor.GOLD).append(f); return this; }

    public final ColorBuilder gray(final float f) {
        sb.append(ChatColor.GRAY).append(f); return this; }

    public final ColorBuilder green(final float f) {
        sb.append(ChatColor.GREEN).append(f); return this; }

    public final ColorBuilder lightpurple(final float f) {
        sb.append(ChatColor.LIGHT_PURPLE).append(f); return this; }

    public final ColorBuilder red(final float f) {
        sb.append(ChatColor.RED).append(f); return this; }

    public final ColorBuilder white(final float f) {
        sb.append(ChatColor.WHITE).append(f); return this; }

    public final ColorBuilder yellow(final float f) {
        sb.append(ChatColor.YELLOW).append(f); return this; }

    public final ColorBuilder bold(final float f) {
        sb.append(ChatColor.BOLD).append(f); return this; }

    public final ColorBuilder italic(final float f) {
        sb.append(ChatColor.ITALIC).append(f); return this; }

    public final ColorBuilder strike(final float f) {
        sb.append(ChatColor.STRIKETHROUGH).append(f); return this; }

    public final ColorBuilder under(final float f) {
        sb.append(ChatColor.UNDERLINE).append(f); return this; }

    public final ColorBuilder reset(final float f) {
        sb.append(ChatColor.RESET).append(f); return this; }

    public final ColorBuilder magic(final float f) {
        sb.append(ChatColor.MAGIC).append(f); return this; }


    public final ColorBuilder aqua(final int i) {
        sb.append(ChatColor.AQUA).append(i); return this; }

    public final ColorBuilder black(final int i) {
        sb.append(ChatColor.BLACK).append(i); return this; }

    public final ColorBuilder blue(final int i) {
        sb.append(ChatColor.BLUE).append(i); return this; }

    public final ColorBuilder darkaqua(final int i) {
        sb.append(ChatColor.DARK_AQUA).append(i); return this; }

    public final ColorBuilder darkblue(final int i) {
        sb.append(ChatColor.DARK_BLUE).append(i); return this; }

    public final ColorBuilder darkgray(final int i) {
        sb.append(ChatColor.DARK_GRAY).append(i); return this; }

    public final ColorBuilder darkgreen(final int i) {
        sb.append(ChatColor.DARK_GREEN).append(i); return this; }

    public final ColorBuilder darkpurple(final int i) {
        sb.append(ChatColor.DARK_PURPLE).append(i); return this; }

    public final ColorBuilder darkred(final int i) {
        sb.append(ChatColor.DARK_RED).append(i); return this; }

    public final ColorBuilder gold(final int i) {
        sb.append(ChatColor.GOLD).append(i); return this; }

    public final ColorBuilder gray(final int i) {
        sb.append(ChatColor.GRAY).append(i); return this; }

    public final ColorBuilder green(final int i) {
        sb.append(ChatColor.GREEN).append(i); return this; }

    public final ColorBuilder lightpurple(final int i) {
        sb.append(ChatColor.LIGHT_PURPLE).append(i); return this; }

    public final ColorBuilder red(final int i) {
        sb.append(ChatColor.RED).append(i); return this; }

    public final ColorBuilder white(final int i) {
        sb.append(ChatColor.WHITE).append(i); return this; }

    public final ColorBuilder yellow(final int i) {
        sb.append(ChatColor.YELLOW).append(i); return this; }

    public final ColorBuilder bold(final int i) {
        sb.append(ChatColor.BOLD).append(i); return this; }

    public final ColorBuilder italic(final int i) {
        sb.append(ChatColor.ITALIC).append(i); return this; }

    public final ColorBuilder strike(final int i) {
        sb.append(ChatColor.STRIKETHROUGH).append(i); return this; }

    public final ColorBuilder under(final int i) {
        sb.append(ChatColor.UNDERLINE).append(i); return this; }

    public final ColorBuilder reset(final int i) {
        sb.append(ChatColor.RESET).append(i); return this; }

    public final ColorBuilder magic(final int i) {
        sb.append(ChatColor.MAGIC).append(i); return this; }


    public final ColorBuilder aqua(final long lng) {
        sb.append(ChatColor.AQUA).append(lng); return this; }

    public final ColorBuilder black(final long lng) {
        sb.append(ChatColor.BLACK).append(lng); return this; }

    public final ColorBuilder blue(final long lng) {
        sb.append(ChatColor.BLUE).append(lng); return this; }

    public final ColorBuilder darkaqua(final long lng) {
        sb.append(ChatColor.DARK_AQUA).append(lng); return this; }

    public final ColorBuilder darkblue(final long lng) {
        sb.append(ChatColor.DARK_BLUE).append(lng); return this; }

    public final ColorBuilder darkgray(final long lng) {
        sb.append(ChatColor.DARK_GRAY).append(lng); return this; }

    public final ColorBuilder darkgreen(final long lng) {
        sb.append(ChatColor.DARK_GREEN).append(lng); return this; }

    public final ColorBuilder darkpurple(final long lng) {
        sb.append(ChatColor.DARK_PURPLE).append(lng); return this; }

    public final ColorBuilder darkred(final long lng) {
        sb.append(ChatColor.DARK_RED).append(lng); return this; }

    public final ColorBuilder gold(final long lng) {
        sb.append(ChatColor.GOLD).append(lng); return this; }

    public final ColorBuilder gray(final long lng) {
        sb.append(ChatColor.GRAY).append(lng); return this; }

    public final ColorBuilder green(final long lng) {
        sb.append(ChatColor.GREEN).append(lng); return this; }

    public final ColorBuilder lightpurple(final long lng) {
        sb.append(ChatColor.LIGHT_PURPLE).append(lng); return this; }

    public final ColorBuilder red(final long lng) {
        sb.append(ChatColor.RED).append(lng); return this; }

    public final ColorBuilder white(final long lng) {
        sb.append(ChatColor.WHITE).append(lng); return this; }

    public final ColorBuilder yellow(final long lng) {
        sb.append(ChatColor.YELLOW).append(lng); return this; }

    public final ColorBuilder bold(final long lng) {
        sb.append(ChatColor.BOLD).append(lng); return this; }

    public final ColorBuilder italic(final long lng) {
        sb.append(ChatColor.ITALIC).append(lng); return this; }

    public final ColorBuilder strike(final long lng) {
        sb.append(ChatColor.STRIKETHROUGH).append(lng); return this; }

    public final ColorBuilder under(final long lng) {
        sb.append(ChatColor.UNDERLINE).append(lng); return this; }

    public final ColorBuilder reset(final long lng) {
        sb.append(ChatColor.RESET).append(lng); return this; }

    public final ColorBuilder magic(final long lng) {
        sb.append(ChatColor.MAGIC).append(lng); return this; }


    public final ColorBuilder aqua(final Object obj) {
        sb.append(ChatColor.AQUA).append(obj); return this; }

    public final ColorBuilder black(final Object obj) {
        sb.append(ChatColor.BLACK).append(obj); return this; }

    public final ColorBuilder blue(final Object obj) {
        sb.append(ChatColor.BLUE).append(obj); return this; }

    public final ColorBuilder darkaqua(final Object obj) {
        sb.append(ChatColor.DARK_AQUA).append(obj); return this; }

    public final ColorBuilder darkblue(final Object obj) {
        sb.append(ChatColor.DARK_BLUE).append(obj); return this; }

    public final ColorBuilder darkgray(final Object obj) {
        sb.append(ChatColor.DARK_GRAY).append(obj); return this; }

    public final ColorBuilder darkgreen(final Object obj) {
        sb.append(ChatColor.DARK_GREEN).append(obj); return this; }

    public final ColorBuilder darkpurple(final Object obj) {
        sb.append(ChatColor.DARK_PURPLE).append(obj); return this; }

    public final ColorBuilder darkred(final Object obj) {
        sb.append(ChatColor.DARK_RED).append(obj); return this; }

    public final ColorBuilder gold(final Object obj) {
        sb.append(ChatColor.GOLD).append(obj); return this; }

    public final ColorBuilder gray(final Object obj) {
        sb.append(ChatColor.GRAY).append(obj); return this; }

    public final ColorBuilder green(final Object obj) {
        sb.append(ChatColor.GREEN).append(obj); return this; }

    public final ColorBuilder lightpurple(final Object obj) {
        sb.append(ChatColor.LIGHT_PURPLE).append(obj); return this; }

    public final ColorBuilder red(final Object obj) {
        sb.append(ChatColor.RED).append(obj); return this; }

    public final ColorBuilder white(final Object obj) {
        sb.append(ChatColor.WHITE).append(obj); return this; }

    public final ColorBuilder yellow(final Object obj) {
        sb.append(ChatColor.YELLOW).append(obj); return this; }

    public final ColorBuilder bold(final Object obj) {
        sb.append(ChatColor.BOLD).append(obj); return this; }

    public final ColorBuilder italic(final Object obj) {
        sb.append(ChatColor.ITALIC).append(obj); return this; }

    public final ColorBuilder strike(final Object obj) {
        sb.append(ChatColor.STRIKETHROUGH).append(obj); return this; }

    public final ColorBuilder under(final Object obj) {
        sb.append(ChatColor.UNDERLINE).append(obj); return this; }

    public final ColorBuilder reset(final Object obj) {
        sb.append(ChatColor.RESET).append(obj); return this; }

    public final ColorBuilder magic(final Object obj) {
        sb.append(ChatColor.MAGIC).append(obj); return this; }


    public final ColorBuilder aqua(final String str) {
        sb.append(ChatColor.AQUA).append(str); return this; }

    public final ColorBuilder black(final String str) {
        sb.append(ChatColor.BLACK).append(str); return this; }

    public final ColorBuilder blue(final String str) {
        sb.append(ChatColor.BLUE).append(str); return this; }

    public final ColorBuilder darkaqua(final String str) {
        sb.append(ChatColor.DARK_AQUA).append(str); return this; }

    public final ColorBuilder darkblue(final String str) {
        sb.append(ChatColor.DARK_BLUE).append(str); return this; }

    public final ColorBuilder darkgray(final String str) {
        sb.append(ChatColor.DARK_GRAY).append(str); return this; }

    public final ColorBuilder darkgreen(final String str) {
        sb.append(ChatColor.DARK_GREEN).append(str); return this; }

    public final ColorBuilder darkpurple(final String str) {
        sb.append(ChatColor.DARK_PURPLE).append(str); return this; }

    public final ColorBuilder darkred(final String str) {
        sb.append(ChatColor.DARK_RED).append(str); return this; }

    public final ColorBuilder gold(final String str) {
        sb.append(ChatColor.GOLD).append(str); return this; }

    public final ColorBuilder gray(final String str) {
        sb.append(ChatColor.GRAY).append(str); return this; }

    public final ColorBuilder green(final String str) {
        sb.append(ChatColor.GREEN).append(str); return this; }

    public final ColorBuilder lightpurple(final String str) {
        sb.append(ChatColor.LIGHT_PURPLE).append(str); return this; }

    public final ColorBuilder red(final String str) {
        sb.append(ChatColor.RED).append(str); return this; }

    public final ColorBuilder white(final String str) {
        sb.append(ChatColor.WHITE).append(str); return this; }

    public final ColorBuilder yellow(final String str) {
        sb.append(ChatColor.YELLOW).append(str); return this; }

    public final ColorBuilder bold(final String str) {
        sb.append(ChatColor.BOLD).append(str); return this; }

    public final ColorBuilder italic(final String str) {
        sb.append(ChatColor.ITALIC).append(str); return this; }

    public final ColorBuilder strike(final String str) {
        sb.append(ChatColor.STRIKETHROUGH).append(str); return this; }

    public final ColorBuilder under(final String str) {
        sb.append(ChatColor.UNDERLINE).append(str); return this; }

    public final ColorBuilder reset(final String str) {
        sb.append(ChatColor.RESET).append(str); return this; }

    public final ColorBuilder magic(final String str) {
        sb.append(ChatColor.MAGIC).append(str); return this; }


    public final ColorBuilder aqua(final StringBuffer sb) {
        sb.append(ChatColor.AQUA).append(sb); return this; }

    public final ColorBuilder black(final StringBuffer sb) {
        sb.append(ChatColor.BLACK).append(sb); return this; }

    public final ColorBuilder blue(final StringBuffer sb) {
        sb.append(ChatColor.BLUE).append(sb); return this; }

    public final ColorBuilder darkaqua(final StringBuffer sb) {
        sb.append(ChatColor.DARK_AQUA).append(sb); return this; }

    public final ColorBuilder darkblue(final StringBuffer sb) {
        sb.append(ChatColor.DARK_BLUE).append(sb); return this; }

    public final ColorBuilder darkgray(final StringBuffer sb) {
        sb.append(ChatColor.DARK_GRAY).append(sb); return this; }

    public final ColorBuilder darkgreen(final StringBuffer sb) {
        sb.append(ChatColor.DARK_GREEN).append(sb); return this; }

    public final ColorBuilder darkpurple(final StringBuffer sb) {
        sb.append(ChatColor.DARK_PURPLE).append(sb); return this; }

    public final ColorBuilder darkred(final StringBuffer sb) {
        sb.append(ChatColor.DARK_RED).append(sb); return this; }

    public final ColorBuilder gold(final StringBuffer sb) {
        sb.append(ChatColor.GOLD).append(sb); return this; }

    public final ColorBuilder gray(final StringBuffer sb) {
        sb.append(ChatColor.GRAY).append(sb); return this; }

    public final ColorBuilder green(final StringBuffer sb) {
        sb.append(ChatColor.GREEN).append(sb); return this; }

    public final ColorBuilder lightpurple(final StringBuffer sb) {
        sb.append(ChatColor.LIGHT_PURPLE).append(sb); return this; }

    public final ColorBuilder red(final StringBuffer sb) {
        sb.append(ChatColor.RED).append(sb); return this; }

    public final ColorBuilder white(final StringBuffer sb) {
        sb.append(ChatColor.WHITE).append(sb); return this; }

    public final ColorBuilder yellow(final StringBuffer sb) {
        sb.append(ChatColor.YELLOW).append(sb); return this; }

    public final ColorBuilder bold(final StringBuffer sb) {
        sb.append(ChatColor.BOLD).append(sb); return this; }

    public final ColorBuilder italic(final StringBuffer sb) {
        sb.append(ChatColor.ITALIC).append(sb); return this; }

    public final ColorBuilder strike(final StringBuffer sb) {
        sb.append(ChatColor.STRIKETHROUGH).append(sb); return this; }

    public final ColorBuilder under(final StringBuffer sb) {
        sb.append(ChatColor.UNDERLINE).append(sb); return this; }

    public final ColorBuilder reset(final StringBuffer sb) {
        sb.append(ChatColor.RESET).append(sb); return this; }

    public final ColorBuilder magic(final StringBuffer sb) {
        sb.append(ChatColor.MAGIC).append(sb); return this; }


    ///////////////////////////////////////////////////////////////////////////
    // Appenders
    ///////////////////////////////////////////////////////////////////////////

    public final ColorBuilder append(final String format, final Object... args) {
        sb.append(String.format(format, args)); return this; }

    public final ColorBuilder append(final boolean b) {
        sb.append(b);
        return this;
    }

    public final ColorBuilder append(final char c) {
        sb.append(c);
        return this;
    }

    public final ColorBuilder append(final char[] str) {
        sb.append(str);
        return this;
    }

    public final ColorBuilder append(final char[] str, final int offset, final int len) {
        sb.append(str, offset, len);
        return this;
    }

    public final ColorBuilder append(final CharSequence s) {
        sb.append(s);
        return this;
    }

    public final ColorBuilder append(final CharSequence s, final int start, final int end) {
        sb.append(s, start, end);
        return this;
    }

    public final ColorBuilder append(final double d) {
        sb.append(d);
        return this;
    }

    public final ColorBuilder append(final float f) {
        sb.append(f);
        return this;
    }

    public final ColorBuilder append(final int i) {
        sb.append(i);
        return this;
    }

    public final ColorBuilder append(final long lng) {
        sb.append(lng);
        return this;
    }

    public final ColorBuilder append(final Object obj) {
        sb.append(obj);
        return this;
    }

    public final ColorBuilder append(final String str) {
        sb.append(str);
        return this;
    }

    public final ColorBuilder append(final StringBuffer sb) {
        sb.append(sb);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final boolean b) {
        sb.append(color).append(b);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final char c) {
        sb.append(color).append(c);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final char[] str) {
        sb.append(color).append(str);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final char[] str, final int offset, final int len) {
        sb.append(color).append(str, offset, len);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final CharSequence s) {
        sb.append(color).append(s);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final CharSequence s, final int start, final int end) {
        sb.append(color).append(s, start, end);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final double d) {
        sb.append(color).append(d);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final float f) {
        sb.append(color).append(f);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final int i) {
        sb.append(color).append(i);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final long lng) {
        sb.append(color).append(lng);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final Object obj) {
        sb.append(color).append(obj);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final String str) {
        sb.append(color).append(str);
        return this;
    }

    public final ColorBuilder append(final ChatColor color, final StringBuffer sb) {
        sb.append(color).append(sb);
        return this;
    }

    public final ColorBuilder appendCodePoint(final int codePoint) {
        sb.appendCodePoint(codePoint);
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Other Stuff
    ///////////////////////////////////////////////////////////////////////////

    public final int capacity() {
        return sb.capacity();
    }

    public final char charAt(final int index) {
        return sb.charAt(index);
    }

    public final int codePointAt(final int index) {
        return sb.codePointAt(index);
    }

    public final int codePointBefore(final int index) {
        return sb.codePointBefore(index);
    }

    public final int codePointCount(final int beginIndex, final int endIndex) {
        return sb.codePointCount(beginIndex, endIndex);
    }

    public final ColorBuilder delete(final int start, final int end) {
        sb.delete(start, end);
        return this;
    }

    public final ColorBuilder deleteCharAt(final int index) {
        sb.deleteCharAt(index);
        return this;
    }

    public final void ensureCapacity(final int minimumCapacity) {
        sb.ensureCapacity(minimumCapacity);
    }

    public final void getChars(final int srcBegin, final int srcEnd, final char[] dst, final int dstBegin) {
        sb.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    public final int indexOf(final String str) {
        return sb.indexOf(str);
    }

    public final int indexOf(final String str, final int fromIndex) {
        return sb.indexOf(str, fromIndex);
    }

    public final int lastIndexOf(final String str) {
        return sb.lastIndexOf(str);
    }

    public final int lastIndexOf(final String str, final int fromIndex) {
        return sb.lastIndexOf(str, fromIndex);
    }

    public final int length() {
        return sb.length();
    }

    public final int offsetByCodePoints(final int index, final int codePointOffset) {
        return sb.offsetByCodePoints(index, codePointOffset);
    }

    public final ColorBuilder replace(final int start, final int end, final String str) {
        sb.replace(start, end, str);
        return this;
    }

    public final ColorBuilder reverse() {
        sb.reverse();
        return this;
    }

    public final void setCharAt(final int index, final char ch) {
        sb.setCharAt(index, ch);
    }

    public final void setLength(final int newLength) {
        sb.setLength(newLength);
    }

    public final CharSequence subSequence(final int start, final int end) {
        return sb.subSequence(start, end);
    }

    public final String substring(final int start) {
        return sb.substring(start);
    }

    public final String substring(final int start, final int end) {
        return sb.substring(start, end);
    }

    public final String toString() {
        return sb.toString();
    }

    public final void trimToSize() {
        sb.trimToSize();
    }

}