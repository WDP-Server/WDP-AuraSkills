package dev.aurelium.auraskills.common.message.type;

import dev.aurelium.auraskills.common.message.MessageKey;

public enum SkillCoinsMessage implements MessageKey {

    SHOP_UNAVAILABLE,
    SHOP_NO_SECTIONS,
    BALANCE_HEADER,
    BALANCE_COINS,
    BALANCE_TOKENS,
    BALANCE_FOOTER,
    BALANCE_OTHER_HEADER,
    AMOUNT_POSITIVE,
    AMOUNT_NEGATIVE,
    INVALID_CURRENCY,
    PLAYER_NOT_FOUND,
    GIVE_SUCCESS,
    GIVE_RECEIVED,
    TAKE_SUCCESS,
    TAKE_LOST,
    SET_SUCCESS;

    @Override
    public String getPath() {
        return "skillcoins." + this.name().toLowerCase();
    }

}
