package io.github.nahkd123.inkingcraft.client.input;

import io.github.nahkd123.inking.api.tablet.Packet;

public record FilteredPacketData(Packet raw, double screenX, double screenY, double pressure) {
}
