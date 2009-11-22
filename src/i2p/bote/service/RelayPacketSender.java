/**
 * Copyright (C) 2009  HungryHobo@mail.i2p
 * 
 * The GPG fingerprint for HungryHobo@mail.i2p is:
 * 6DD3 EAA2 9990 29BC 4AD2 7486 1E2C 7B61 76DC DC12
 * 
 * This file is part of I2P-Bote.
 * I2P-Bote is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * I2P-Bote is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with I2P-Bote.  If not, see <http://www.gnu.org/licenses/>.
 */

package i2p.bote.service;

import i2p.bote.Configuration;
import i2p.bote.folder.PacketFolder;
import i2p.bote.network.I2PSendQueue;
import i2p.bote.packet.RelayPacket;

import java.text.ParseException;

import com.nettgryppa.security.HashCash;

import net.i2p.I2PAppContext;
import net.i2p.crypto.ElGamalAESEngine;
import net.i2p.crypto.SessionKeyManager;
import net.i2p.util.Log;
import net.i2p.util.RandomSource;

/**
 * A background thread that sends packets in the relay outbox to the I2P network.
 */
public class RelayPacketSender extends I2PBoteThread {
    private static final int PAUSE = 10 * 60 * 1000;   // the wait time, in milliseconds,  before processing the folder again
    private static final int PADDED_SIZE = 16 * 1024;
    private static final Log log = new Log(RelayPacketSender.class);
    
    private I2PSendQueue sendQueue;
    private ElGamalAESEngine encrypter = I2PAppContext.getGlobalContext().elGamalAESEngine();
    private SessionKeyManager sessionKeyManager = I2PAppContext.getGlobalContext().sessionKeyManager();
    private PacketFolder<RelayPacket> packetStore;
    private Configuration configuration;
    
    public RelayPacketSender(I2PSendQueue sendQueue, PacketFolder<RelayPacket> packetStore) {
        super("RelayPacketSender");
        this.sendQueue = sendQueue;
        this.packetStore = packetStore;
    }
    
    @Override
    public void run() {
        while (true) {
            if (log.shouldLog(Log.DEBUG))
                log.debug("Deleting expired packets...");
            try {
                deleteExpiredPackets();
            } catch (Exception e) {
                log.error("Error deleting expired packets", e);
            }
            
            log.info("Processing outgoing packets in directory '" + packetStore.getStorageDirectory().getAbsolutePath() + "'");
            for (RelayPacket packet: packetStore) {
                log.info("Processing packet file: <" + packet.getFile() + ">");
                try {
                    HashCash hashCash = null;   // TODO
                    long sendTime = getRandomSendTime(packet);
                    sendQueue.sendRelayRequest(packet, hashCash, sendTime);
                } catch (Exception e) {
                    log.error("Error sending packet. ", e);
                }
            }
            
            try {
                Thread.sleep(PAUSE);
            } catch (InterruptedException e) {
                log.error("RelayPacketSender received an InterruptedException.");
            }
        }
    }
    
    private long getRandomSendTime(RelayPacket packet) {
        long min = packet.getEarliestSendTime();
        long max = packet.getLatestSendTime();
        return min + RandomSource.getInstance().nextLong(max-min);
    }
    
    public void deleteExpiredPackets() throws ParseException {
        // TODO look at filename which = receive time, delete if too old
    }
}