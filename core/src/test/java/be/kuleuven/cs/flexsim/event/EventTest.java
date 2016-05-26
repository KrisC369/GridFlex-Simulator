package be.kuleuven.cs.flexsim.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import javax.naming.directory.NoSuchAttributeException;

import org.junit.Test;

/**
 * @author Rutger Claes <rutger.claes@cs.kuleuven.be>
 */
public class EventTest {

    /**
     * Test of getType method, of class Event.
     */
    @Test
    public void testGetType() {
        String type = "foo:bar";
        Event e = new EventFactoryImplementation().build(type);
        assertEquals(type, e.getType());
    }

    /**
     * Test of clearAttributes method, of class Event.
     */
    @Test
    public void testClearAttributes() {
        Event e = new Event("test:event");
        for (int i = 0; i < 5; i++) {
            e.setAttribute("" + i, i);
            assertTrue(e.hasAttribute("" + i));
        }

        e.clearAttributes();
        for (int i = 0; i < 5; i++) {
            assertFalse(e.hasAttribute("" + i));
        }

        assertEquals("test:event", e.getType());
    }

    /**
     * Test of getAttribute method, of class Event.
     */
    @Test
    public void testGetAndSetAttribute() {
        Event e = new Event("test:event");

        for (Integer i = 0; i < 5; i++) {
            e.setAttribute(i.toString(), i);
            for (Integer j = 0; j <= i; j++) {
                assertTrue(e.hasAttribute(j.toString()));
                assertTrue(e.hasAttribute(j.toString(), Integer.class));
                try {
                    assertEquals(j,
                            e.getAttribute(j.toString(), Integer.class));
                } catch (NoSuchAttributeException e1) {
                    fail();
                }
            }
            for (Integer k = i + 1; k < 5; k++) {
                assertFalse(e.hasAttribute(k.toString()));
                try {
                    assertNull(e.getAttribute(k.toString(), Object.class));
                    fail("Exception expected");
                } catch (NoSuchAttributeException e1) {
                    // correct to throw exception here.
                }
            }
        }

        for (Integer i = 0; i < 5; i++) {
            e.setAttribute(i.toString(), 5 - i);
            try {
                assertEquals((Integer) (5 - i),
                        e.getAttribute(i.toString(), Integer.class));
            } catch (NoSuchAttributeException e1) {
                fail();
            }
        }
    }

    /**
     * Test of getAttributes method, of class Event.
     */
    @Test
    public void testGetAttributes() {
        Event e = new Event("test:event");
        for (Integer i = 0; i < 5; i++) {
            e.setAttribute(i.toString(), i);
        }

        Map<String, Object> attributes = e.getAttributes();
        assertEquals(6, attributes.size());

        assertTrue(attributes.containsKey("type"));
        assertEquals("test:event", attributes.get("type"));

        for (Integer i = 0; i < 5; i++) {
            assertTrue(attributes.containsKey(i.toString()));
            assertEquals(i, attributes.get(i.toString()));
        }

        try {
            attributes.put("6", 6);
            fail("Attributes are changeable");
        } catch (UnsupportedOperationException ex) {
            // OK
        }
    }

    /**
     * Test of setAttribute method, of class Event. Setting and resetting is
     * already covered by {@link EventTest#testGetAndSetAttribute()}
     */
    @Test
    public void testSetAttribute() {
        {
            // Test setting of special type attribute
            Event e = new Event("test:event");
            try {
                e.setAttribute("type", "other:type");
                fail("No exception thrown");
            } catch (IllegalArgumentException ex) {
                // OK
                assertEquals("test:event", e.getType());
                assertEquals("test:event", e.getAttributes().get("type"));
            }
        }
    }

    /**
     * Test of setAttributes method, of class Event.
     */
    @Test
    public void testSetAttributes() {
        {
            Event e = new Event("test:type");
            Map<String, Object> futureAttributes = new HashMap<>();
            for (Integer i = 0; i < 5; i++) {
                if (i <= 2) {
                    e.setAttribute(i.toString(), i);
                }
                if (i >= 2) {
                    futureAttributes.put(i.toString(), 5 - i);
                }
            }

            e.setAttributes(futureAttributes);
            for (Integer i = 0; i < 5; i++) {
                if (i < 2) {
                    assertFalse(e.hasAttribute(i.toString()));
                }
                if (i >= 2) {
                    assertTrue(e.hasAttribute(i.toString()));
                    try {
                        assertEquals((Integer) (5 - i),
                                e.getAttribute(i.toString(), Integer.class));
                    } catch (NoSuchAttributeException e1) {
                        fail();
                    }
                }
            }
        }

        {
            Event e = new Event("test:type");
            e.setAttribute("foo", "bar");
            Map<String, Object> attributes = new HashMap<String, Object>();
            attributes.put("type", "other:type");
            attributes.put("foo", "baz");
            attributes.put("boz", "box");
            try {
                e.setAttributes(attributes);
            } catch (IllegalArgumentException ex) {
                assertEquals("test:type", e.getType());
                try {
                    assertEquals("bar", e.getAttribute("foo", String.class));
                } catch (NoSuchAttributeException e1) {
                    fail();
                }
                assertFalse(e.hasAttribute("boz"));
            }
        }
    }

    /**
     * Test of hasAttribute method, of class Event.
     */
    @Test
    public void testHasAttributeByKey() {
        Event e = new Event("test:event");
        e.setAttribute("foo", "bar");
        e.setAttribute("boz", "bax");

        assertTrue(e.hasAttribute("foo"));
        assertTrue(e.hasAttribute("boz"));
        assertFalse(e.hasAttribute("bar"));
    }

    /**
     * Test of hasAttribute method, of class Event.
     */
    @Test
    public void testHasAttributeByType() {
        Event e = new Event("test:event");
        e.setAttribute("number", 1);
        e.setAttribute("string", "foo");
        e.setAttribute("boolean", true);

        assertTrue(e.hasAttribute("number"));
        assertTrue(e.hasAttribute("number", Number.class));
        assertTrue(e.hasAttribute("number", Object.class));

        assertFalse(e.hasAttribute("number", String.class));
        assertFalse(e.hasAttribute("numbr", Number.class));
        assertFalse(e.hasAttribute("numbr", Object.class));

        assertTrue(e.hasAttribute("string", String.class));
        assertTrue(e.hasAttribute("string", Object.class));
        assertFalse(e.hasAttribute("string", Number.class));

        assertTrue(e.hasAttribute("boolean", Boolean.class));
        assertTrue(e.hasAttribute("boolean", Object.class));
        assertFalse(e.hasAttribute("boolean", String.class));
    }
}
