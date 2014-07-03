package com.timgroup.tickets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

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

    @Test public void marshals_single_attribute_with_empty_value() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.add('a', "");
        assertEquals("a,xnoodles", ticket.marshal());
    }

    @Test public void marshals_single_attribute_with_value() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.add('a', "1");
        assertEquals("a1,xnoodles", ticket.marshal());
    }

    @Test public void marshals_two_attributes_in_insertion_order() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.add('b', "2");
        ticket.add('a', "1");
        assertEquals("b2,a1,xnoodles", ticket.marshal());
    }

    @Test public void marshals_multiple_attribute_values() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.add('a', "1");
        ticket.add('a', "2");
        assertEquals("a1,a2,xnoodles", ticket.marshal());
    }

    @Test public void marshals_attribute_values_grouped_together() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.add('a', "1");
        ticket.add('b', "1");
        ticket.add('a', "2");
        assertEquals("a1,a2,b1,xnoodles", ticket.marshal());
    }

    @Test public void unmarshals_just_mac_to_empty_ticket() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal("xnoodles");
        assertThat(ticket, is(emptyTicket()));
    }

    @Test public void unmarshals_single_attribute_with_empty_value() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal("a,xnoodles");
        assertThat(ticket, is(ticket().containing('a', "")));
    }

    @Test public void unmarshals_single_attribute_with_value() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal("a1,xnoodles");
        assertThat(ticket, is(ticket().containing('a', "1")));
    }

    @Test public void unmarshals_two_attributes_in_order() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal("a1,b1,xnoodles");
        assertThat(ticket, is(ticket().containing('a', "1").containing('b', "1").inOrder()));
    }

    @Test public void unmarshals_attributes_even_if_not_grouped_correctly() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal("a1,b1,a2,xnoodles");
        assertThat(ticket, is(ticket().containing('a', "1", "2").containing('b', "1")));
    }

    @Test public void unmarshalling_ignores_empty_parts() throws Exception {
        Ticket ticket = new Ticket(DUMMY_MAC_GENERATOR);
        ticket.unmarshal(",xnoodles");
        assertThat(ticket, is(emptyTicket()));
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
