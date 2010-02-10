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

package i2p.bote.web;

import i2p.bote.I2PBote;
import i2p.bote.network.BannedPeer;
import i2p.bote.network.DhtPeerStats;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import net.i2p.util.Log;

public class PeerInfoTag extends SimpleTagSupport {
    private Log log = new Log(PeerInfoTag.class);

    public void doTag() {
        PageContext pageContext = (PageContext) getJspContext();
        JspWriter out = pageContext.getOut();
        
        try {
            // Print DHT peer info
            DhtPeerStats dhtStats = I2PBote.getInstance().getDhtStats();
            int numPeers = dhtStats.getData().size();
            out.println("<strong>K-Peers: " + numPeers + "</strong>");
            if (numPeers > 0) {
                out.println("<table>");
                
                // header
                out.println("<tr>");
                for (String columnHeader: dhtStats.getHeader())
                    out.println("<th>" + columnHeader + "</th>");
                out.println("</tr>");
                
                // data
                for (List<String> row: dhtStats.getData()) {
                    out.println("<tr>");
                    for (String cellData: row)
                        out.println("<td>" + cellData + "</td>");
                    out.println("</tr>");
                }
                
                out.println("</table>");
            }
            
            out.println("<p/><br/>");
            
            // List banned peers
            Collection<BannedPeer> bannedPeers = I2PBote.getInstance().getBannedPeers();
            out.println("<strong>Banned Peers: " + bannedPeers.size() + "</strong>");
            if (bannedPeers.size() > 0) {
                out.println("<table>");
                out.println("<tr>");
                out.println("<th>Peer</th>");
                out.println("<th>Destination Hash</th>");
                out.println("<th>Ban Reason</th>");
                out.println("</tr>");
                
                int peerIndex = 1;
                for (BannedPeer peer: bannedPeers) {
                    out.println("<tr>");
                    out.println("<td>" + peerIndex++ + "</td>");
                    out.println("<td>" + peer.getDestination().calculateHash().toBase64() + "</td>");
                    out.println("<td>" + (peer.getBanReason()==null?"":peer.getBanReason()) + "</td>");
                    out.println("</tr>");
                }
                
                out.println("</table>");
            }
        } catch (IOException e) {
            log.error("Can't write output to HTML page", e);
        }
    }
}