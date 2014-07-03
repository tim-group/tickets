package com.timgroup.tickets;

import org.junit.Test;

import com.timgroup.tickets.InvalidTicketException;
import com.timgroup.tickets.Ticket;
import com.timgroup.tickets.TicketMacGenerator;
import com.timgroup.tickets.TicketMacMismatchException;

import static org.junit.Assert.*;

public class TicketTest {
    private static final TicketMacGenerator DUMMY_MAC_GENERATOR = new TicketMacGenerator() {
        @Override public String generateMAC(String input) {
            return "noodles";
        }
    };

    @Test public void marshals_empty_ticket() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        assertEquals("xnoodles", ticket.marshal());
    }

    @Test public void unmarshals_just_mac_to_empty_ticket() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal("xnoodles");
    }

    @Test public void unmarshals_single_attribute() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal("a1,xnoodles");
    }

    @Test(expected = InvalidTicketException.class) public void fails_to_unmarshal_empty_string() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal("");
    }

    @Test(expected = TicketMacMismatchException.class) public void fails_to_unmarshal_just_invalid_mac() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal("xblahblah");
    }

    @Test(expected = TicketMacMismatchException.class) public void fails_to_unmarshal_attribute_and_invalid_mac() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal("a1,xblahblah");
    }
}
