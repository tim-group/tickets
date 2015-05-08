package com.timgroup.tickets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

public class TicketFactoryTest {
    private static final TicketMacGenerator DUMMY_MAC_GENERATOR = new TicketMacGenerator() {
        @Override public String generateMAC(String input) {
            return "noodles";
        }
    };

    @Test public void marshals_empty_ticket() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        Ticket ticket = new Ticket();
        assertEquals("xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void marshals_single_attribute_with_empty_value() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        Ticket ticket = new Ticket();
        ticket.add('a', "");
        assertEquals("a,xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void marshals_using_specified_separator() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR, '.', '=', '+', "".toCharArray());
        Ticket ticket = new Ticket();
        ticket.add('a', "");
        assertEquals("a.xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void escapes_specified_separator() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR, '.', '=', '+', "".toCharArray());
        Ticket ticket = new Ticket();
        ticket.add('a', "b.c");
        assertEquals("ab+2ec.xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void escapes_specified_unicode_escape() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR, '.', '!', '+', "".toCharArray());
        Ticket ticket = new Ticket();
        ticket.add('a', "b!c");
        assertEquals("ab+21c.xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void escapes_specified_reserved_character_escape() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR, '.', '=', '!', "".toCharArray());
        Ticket ticket = new Ticket();
        ticket.add('a', "b!c");
        assertEquals("ab!21c.xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void marshals_single_attribute_with_value() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        Ticket ticket = new Ticket();
        ticket.add('a', "1");
        assertEquals("a1,xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void marshals_two_attributes_in_insertion_order() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        Ticket ticket = new Ticket();
        ticket.add('b', "2");
        ticket.add('a', "1");
        assertEquals("b2,a1,xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void marshals_multiple_attribute_values() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        Ticket ticket = new Ticket();
        ticket.add('a', "1");
        ticket.add('a', "2");
        assertEquals("a1,a2,xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void marshals_attribute_values_grouped_together() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        Ticket ticket = new Ticket();
        ticket.add('a', "1");
        ticket.add('b', "1");
        ticket.add('a', "2");
        assertEquals("a1,a2,b1,xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void escapes_single_reserved_ascii_characters_when_marshalling() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        Ticket ticket = new Ticket();
        ticket.add('a', "1,2");
        ticket.add('a', "1 2");
        ticket.add('a', "1+2");
        ticket.add('a', "1=2");
        ticket.add('a', "1\n2");
        assertEquals("a1+2c2,a1+202,a1+2b2,a1+3d2,a1+0a2,xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void escapes_other_unicode_characters_when_marshalling() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        Ticket ticket = new Ticket();
        ticket.add('a', "\u00a3");
        ticket.add('a', "\u20ac");
        assertEquals("a+a3,a=20ac,xnoodles", ticketFactory.marshal(ticket));
    }

    @Test public void unmarshals_just_mac_to_empty_ticket() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        assertThat(ticketFactory.unmarshal("xnoodles"), is(emptyTicket()));
    }

    @Test public void unmarshals_single_attribute_with_empty_value() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        assertThat(ticketFactory.unmarshal("a,xnoodles"), is(ticket().containing('a', "")));
    }

    @Test public void unmarshals_using_specified_separator() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR, '.', '=', '+', "".toCharArray());
        assertThat(ticketFactory.unmarshal("a.xnoodles"), is(ticket().containing('a', "")));
    }

    @Test public void unmarshals_single_attribute_with_value() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        assertThat(ticketFactory.unmarshal("a1,xnoodles"), is(ticket().containing('a', "1")));
    }

    @Test public void unmarshals_two_attributes_in_order() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        assertThat(ticketFactory.unmarshal("a1,b1,xnoodles"), is(ticket().containing('a', "1").containing('b', "1").inOrder()));
    }

    @Test public void unmarshals_attributes_even_if_not_grouped_correctly() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        assertThat(ticketFactory.unmarshal("a1,b1,a2,xnoodles"), is(ticket().containing('a', "1", "2").containing('b', "1")));
    }

    @Test public void unmarshalling_ignores_empty_parts() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        assertThat(ticketFactory.unmarshal(",xnoodles"), is(emptyTicket()));
    }

    @Test public void unescapes_ascii_characters_when_unmarshalling() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        assertThat(ticketFactory.unmarshal("a1+2c2,xnoodles"), is(ticket().containing('a', "1,2")));
    }

    @Test public void unescapes_other_unicode_characters_when_unmarshalling() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        assertThat(ticketFactory.unmarshal("a=20ac,xnoodles"), is(ticket().containing('a', "\u20ac")));
    }

    @Test public void unescapes_even_when_hex_formatted_as_uppercase() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        assertThat(ticketFactory.unmarshal("a=20AC,xnoodles"), is(ticket().containing('a', "\u20ac")));
    }

    @Test(expected = InvalidTicketException.class) public void fails_to_unmarshal_empty_string() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        ticketFactory.unmarshal("");
    }

    @Test(expected = TicketMacMismatchException.class) public void fails_to_unmarshal_just_invalid_mac() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        ticketFactory.unmarshal("xblahblah");
    }

    @Test(expected = TicketMacMismatchException.class) public void fails_to_unmarshal_attribute_and_invalid_mac() throws Exception {
        TicketFactory ticketFactory = new TicketFactory(DUMMY_MAC_GENERATOR);
        ticketFactory.unmarshal("a1,xblahblah");
    }

    @Test public void marshalling_passes_empty_payload_to_mac_generator() throws Exception {
        TicketMacGenerator macGenerator = mock(TicketMacGenerator.class);
        when(macGenerator.generateMAC(anyString())).thenReturn("zzzz");
        TicketFactory ticketFactory = new TicketFactory(macGenerator);
        ticketFactory.marshal(new Ticket());
        verify(macGenerator).generateMAC("");
        verifyNoMoreInteractions(macGenerator);
    }

    @Test public void marshalling_passes_payload_without_trailer_to_mac_generator() throws Exception {
        TicketMacGenerator macGenerator = mock(TicketMacGenerator.class);
        when(macGenerator.generateMAC(anyString())).thenReturn("zzzz");
        TicketFactory ticketFactory = new TicketFactory(macGenerator);
        Ticket ticket = new Ticket();
        ticket.add('a', "1");
        ticketFactory.marshal(ticket);
        verify(macGenerator).generateMAC("a1");
        verifyNoMoreInteractions(macGenerator);
    }

    @Test public void unmarshalling_passes_empty_payload_to_mac_generator() throws Exception {
        TicketMacGenerator macGenerator = mock(TicketMacGenerator.class);
        when(macGenerator.generateMAC(anyString())).thenReturn("zzzz");
        TicketFactory ticketFactory = new TicketFactory(macGenerator);
        ticketFactory.unmarshal("xzzzz");
        verify(macGenerator).generateMAC("");
        verifyNoMoreInteractions(macGenerator);
    }

    @Test public void unmarshalling_passes_payload_without_trailer_to_mac_generator() throws Exception {
        TicketMacGenerator macGenerator = mock(TicketMacGenerator.class);
        when(macGenerator.generateMAC(anyString())).thenReturn("zzzz");
        TicketFactory ticketFactory = new TicketFactory(macGenerator);
        ticketFactory.unmarshal("a1,xzzzz");
        verify(macGenerator).generateMAC("a1");
        verifyNoMoreInteractions(macGenerator);
    }

    private static Matcher<Ticket> emptyTicket() {
        return new TypeSafeDiagnosingMatcher<Ticket>(Ticket.class) {
            @Override protected boolean matchesSafely(Ticket item, Description mismatchDescription) {
                if (!item.keySet().isEmpty()) {
                    mismatchDescription.appendText("contains keys ").appendValue(item.keySet());
                    return false;
                }
                return true;
            }

            @Override public void describeTo(Description description) {
                description.appendText("empty ticket");
            }
        };
    }

    private static TicketWithDataMatcher ticket() {
        return new TicketWithDataMatcher();
    }

    private static class TicketWithDataMatcher extends TypeSafeDiagnosingMatcher<Ticket> {
        private final Map<Character, List<String>> expectedData = new LinkedHashMap<Character, List<String>>();
        private boolean strictKeyOrder = true;

        public TicketWithDataMatcher() {
            super(Ticket.class);
        }

        @Override protected boolean matchesSafely(Ticket item, Description mismatchDescription) {
            if (!item.keySet().equals(expectedData.keySet())) {
                mismatchDescription.appendText("ticket contains keys ").appendValue(item.keySet()).appendText(" expected ")
                        .appendValue(expectedData.keySet());
                return false;
            }
            if (strictKeyOrder && !new ArrayList<Character>(item.keySet()).equals(new ArrayList<Character>(expectedData.keySet()))) {
                mismatchDescription.appendText("ticket keys in order ").appendValue(item.keySet()).appendText(" expected ")
                        .appendValue(expectedData.keySet());
                return false;
            }
            for (char key : expectedData.keySet()) {
                List<String> expectedValues = expectedData.get(key);
                List<String> values = item.get(key);
                if (!values.equals(expectedValues)) {
                    mismatchDescription.appendText("ticket values for ").appendValue(key).appendText(" expected ")
                            .appendValue(expectedValues).appendText(" got ").appendValue(values);
                    return false;
                }
            }
            return true;
        }

        @Override public void describeTo(Description description) {
            description.appendText("ticket containining ").appendValue(expectedData);
        }

        public TicketWithDataMatcher containing(char key, String... value) {
            expectedData.put(key, Arrays.asList(value));
            return this;
        }

        public TicketWithDataMatcher inOrder() {
            strictKeyOrder = true;
            return this;
        }
    }

}
