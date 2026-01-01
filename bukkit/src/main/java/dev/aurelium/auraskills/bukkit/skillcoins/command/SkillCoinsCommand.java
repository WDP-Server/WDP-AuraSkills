package dev.aurelium.auraskills.bukkit.skillcoins.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.common.message.type.SkillCoinsMessage;
import dev.aurelium.auraskills.common.skillcoins.CurrencyType;
import dev.aurelium.auraskills.common.util.text.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("skillcoins|sc")
public class SkillCoinsCommand extends BaseCommand {
    
    private final AuraSkills plugin;
    
    public SkillCoinsCommand(AuraSkills plugin) {
        this.plugin = plugin;
    }
    
    @Subcommand("balance|bal")
    @CommandPermission("auraskills.command.skillcoins.balance")
    @Description("Check your SkillCoins balance")
    public void onBalance(Player player) {
        double coins = plugin.getSkillCoinsEconomy().getBalance(player.getUniqueId(), CurrencyType.COINS);
        double tokens = plugin.getSkillCoinsEconomy().getBalance(player.getUniqueId(), CurrencyType.TOKENS);
        
        player.sendMessage(plugin.getMsg(SkillCoinsMessage.BALANCE_HEADER, plugin.getDefaultLanguage()));
        player.sendMessage(TextUtil.replace(plugin.getMsg(SkillCoinsMessage.BALANCE_COINS, plugin.getDefaultLanguage()),
                "{coins}", String.format("%.2f", coins)));
        player.sendMessage(TextUtil.replace(plugin.getMsg(SkillCoinsMessage.BALANCE_TOKENS, plugin.getDefaultLanguage()),
                "{tokens}", String.format("%.2f", tokens)));
        player.sendMessage(plugin.getMsg(SkillCoinsMessage.BALANCE_FOOTER, plugin.getDefaultLanguage()));
    }
    
    @Subcommand("give")
    @CommandPermission("auraskills.command.skillcoins.give")
    @CommandCompletion("@players coins|tokens @nothing")
    @Description("Give SkillCoins or Tokens to a player")
    @Syntax("<player> <coins|tokens> <amount>")
    public void onGive(CommandSender sender, String playerName, String currencyStr, double amount) {
        if (amount <= 0) {
            sender.sendMessage(plugin.getMsg(SkillCoinsMessage.AMOUNT_POSITIVE, plugin.getDefaultLanguage()));
            return;
        }
        
        CurrencyType type;
        try {
            type = CurrencyType.valueOf(currencyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getMsg(SkillCoinsMessage.INVALID_CURRENCY, plugin.getDefaultLanguage()));
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage(plugin.getMsg(SkillCoinsMessage.PLAYER_NOT_FOUND, plugin.getDefaultLanguage()));
            return;
        }
        
        plugin.getSkillCoinsEconomy().addBalance(target.getUniqueId(), type, amount);
        sender.sendMessage(TextUtil.replace(plugin.getMsg(SkillCoinsMessage.GIVE_SUCCESS, plugin.getDefaultLanguage()),
                "{amount}", String.format("%.2f", amount),
                "{currency}", type.getDisplayName(),
                "{player}", playerName));
        
        if (target.isOnline()) {
            target.getPlayer().sendMessage(TextUtil.replace(plugin.getMsg(SkillCoinsMessage.GIVE_RECEIVED, plugin.getDefaultLanguage()),
                    "{amount}", String.format("%.2f", amount),
                    "{currency}", type.getDisplayName()));
        }
    }
    
    @Subcommand("take")
    @CommandPermission("auraskills.command.skillcoins.take")
    @CommandCompletion("@players coins|tokens @nothing")
    @Description("Take SkillCoins or Tokens from a player")
    @Syntax("<player> <coins|tokens> <amount>")
    public void onTake(CommandSender sender, String playerName, String currencyStr, double amount) {
        if (amount <= 0) {
            sender.sendMessage(plugin.getMsg(SkillCoinsMessage.AMOUNT_POSITIVE, plugin.getDefaultLanguage()));
            return;
        }
        
        CurrencyType type;
        try {
            type = CurrencyType.valueOf(currencyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getMsg(SkillCoinsMessage.INVALID_CURRENCY, plugin.getDefaultLanguage()));
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage(plugin.getMsg(SkillCoinsMessage.PLAYER_NOT_FOUND, plugin.getDefaultLanguage()));
            return;
        }
        
        plugin.getSkillCoinsEconomy().subtractBalance(target.getUniqueId(), type, amount);
        sender.sendMessage(TextUtil.replace(plugin.getMsg(SkillCoinsMessage.TAKE_SUCCESS, plugin.getDefaultLanguage()),
                "{amount}", String.format("%.2f", amount),
                "{currency}", type.getDisplayName(),
                "{player}", playerName));
        
        if (target.isOnline()) {
            target.getPlayer().sendMessage(TextUtil.replace(plugin.getMsg(SkillCoinsMessage.TAKE_LOST, plugin.getDefaultLanguage()),
                    "{amount}", String.format("%.2f", amount),
                    "{currency}", type.getDisplayName()));
        }
    }
    
    @Subcommand("set")
    @CommandPermission("auraskills.command.skillcoins.set")
    @CommandCompletion("@players coins|tokens @nothing")
    @Description("Set a player's SkillCoins or Tokens balance")
    @Syntax("<player> <coins|tokens> <amount>")
    public void onSet(CommandSender sender, String playerName, String currencyStr, double amount) {
        if (amount < 0) {
            sender.sendMessage(plugin.getMsg(SkillCoinsMessage.AMOUNT_NEGATIVE, plugin.getDefaultLanguage()));
            return;
        }
        
        CurrencyType type;
        try {
            type = CurrencyType.valueOf(currencyStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.getMsg(SkillCoinsMessage.INVALID_CURRENCY, plugin.getDefaultLanguage()));
            return;
        }
        
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage(plugin.getMsg(SkillCoinsMessage.PLAYER_NOT_FOUND, plugin.getDefaultLanguage()));
            return;
        }
        
        plugin.getSkillCoinsEconomy().setBalance(target.getUniqueId(), type, amount);
        sender.sendMessage(TextUtil.replace(plugin.getMsg(SkillCoinsMessage.SET_SUCCESS, plugin.getDefaultLanguage()),
                "{player}", playerName,
                "{currency}", type.getDisplayName(),
                "{amount}", String.format("%.2f", amount)));
    }
    
    @Subcommand("check")
    @CommandPermission("auraskills.command.skillcoins.check")
    @CommandCompletion("@players")
    @Description("Check another player's balance")
    @Syntax("<player>")
    public void onCheck(CommandSender sender, String playerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        if (target == null || !target.hasPlayedBefore()) {
            sender.sendMessage(plugin.getMsg(SkillCoinsMessage.PLAYER_NOT_FOUND, plugin.getDefaultLanguage()));
            return;
        }
        
        double coins = plugin.getSkillCoinsEconomy().getBalance(target.getUniqueId(), CurrencyType.COINS);
        double tokens = plugin.getSkillCoinsEconomy().getBalance(target.getUniqueId(), CurrencyType.TOKENS);
        
        sender.sendMessage(TextUtil.replace(plugin.getMsg(SkillCoinsMessage.BALANCE_OTHER_HEADER, plugin.getDefaultLanguage()),
                "{player}", playerName));
        sender.sendMessage(TextUtil.replace(plugin.getMsg(SkillCoinsMessage.BALANCE_COINS, plugin.getDefaultLanguage()),
                "{coins}", String.format("%.2f", coins)));
        sender.sendMessage(TextUtil.replace(plugin.getMsg(SkillCoinsMessage.BALANCE_TOKENS, plugin.getDefaultLanguage()),
                "{tokens}", String.format("%.2f", tokens)));
        sender.sendMessage(plugin.getMsg(SkillCoinsMessage.BALANCE_FOOTER, plugin.getDefaultLanguage()));
    }
}
