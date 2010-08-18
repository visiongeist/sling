/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.osgi.installer.it;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.sling.osgi.installer.InstallableResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;

@RunWith(JUnit4TestRunner.class)
/** Test the {@link OsgiInstaller.registerResources} method, which lets a client
 *  supply a new list of resources.
 */
public class RegisterResourcesTest extends OsgiInstallerTestBase {

    @org.ops4j.pax.exam.junit.Configuration
    public static Option[] configuration() {
        return defaultConfiguration();
    }

    @Before
    public void setUp() {
        setupInstaller();
    }

    @After
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void initialRegistrationTest() throws Exception {
        final Object listener = this.startObservingBundleEvents();
        final List<InstallableResource> r = new ArrayList<InstallableResource>();
        r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testB-1.0.jar")));
        r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-needsB.jar")));
        r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testbundle-1.1.jar")));
        r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testbundle-1.2.jar")));

        installer.registerResources(URL_SCHEME, r);

        this.waitForBundleEvents("Bundles must be installed and started.", listener,
                new BundleEvent("osgi-installer-testB", "1.0", org.osgi.framework.BundleEvent.INSTALLED),
                new BundleEvent("osgi-installer-testB", "1.0", org.osgi.framework.BundleEvent.STARTED),
                new BundleEvent("osgi-installer-needsB", "1.0", org.osgi.framework.BundleEvent.INSTALLED),
                new BundleEvent("osgi-installer-needsB", "1.0", org.osgi.framework.BundleEvent.STARTED),
                new BundleEvent("osgi-installer-testbundle", "1.2", org.osgi.framework.BundleEvent.INSTALLED),
                new BundleEvent("osgi-installer-testbundle", "1.2", org.osgi.framework.BundleEvent.STARTED));

        final String info = "After initial registration";
        assertBundle(info, "osgi-installer-testB", "1.0", Bundle.ACTIVE);
        assertBundle(info, "osgi-installer-needsB", "1.0", Bundle.ACTIVE);
        assertBundle(info, "osgi-installer-testbundle", "1.2", Bundle.ACTIVE);
    }

    @Test
    public void removeAndReaddBundlesTest() throws Exception {
        {
            final Object listener = this.startObservingBundleEvents();
            final List<InstallableResource> r = new ArrayList<InstallableResource>();
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testB-1.0.jar")));
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-needsB.jar")));
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testbundle-1.0.jar")));
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testbundle-1.1.jar")));

            installer.registerResources(URL_SCHEME, r);
            this.waitForBundleEvents("Bundles must be installed and started.", listener,
                    new BundleEvent("osgi-installer-testB", "1.0", org.osgi.framework.BundleEvent.INSTALLED),
                    new BundleEvent("osgi-installer-testB", "1.0", org.osgi.framework.BundleEvent.STARTED),
                    new BundleEvent("osgi-installer-needsB", "1.0", org.osgi.framework.BundleEvent.INSTALLED),
                    new BundleEvent("osgi-installer-needsB", "1.0", org.osgi.framework.BundleEvent.STARTED),
                    new BundleEvent("osgi-installer-testbundle", "1.1", org.osgi.framework.BundleEvent.INSTALLED),
                    new BundleEvent("osgi-installer-testbundle", "1.1", org.osgi.framework.BundleEvent.STARTED));

            final String info = "After initial registration";
            assertBundle(info, "osgi-installer-testB", "1.0", Bundle.ACTIVE);
            assertBundle(info, "osgi-installer-needsB", "1.0", Bundle.ACTIVE);
            assertBundle(info, "osgi-installer-testbundle", "1.1", Bundle.ACTIVE);
        }

        {
            // Add test 1.2 in between, to make sure it disappears in next registerResources call
            final Object listener = this.startObservingBundleEvents();
            installer.addResource(URL_SCHEME, getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testbundle-1.2.jar")));
            this.waitForBundleEvents("Bundles must be installed and started.", listener,
                    new BundleEvent("osgi-installer-testbundle", "1.2", org.osgi.framework.BundleEvent.STARTED));
            assertBundle("After adding testbundle V1.2", "osgi-installer-testbundle", "1.2", Bundle.ACTIVE);
        }

        {
            // Add a bundle with different URL scheme - must not be removed by registerResources
            final Object listener = this.startObservingBundleEvents();
            installer.addResource("anotherscheme", new MockInstallableResource(
                    "osgi-installer-testA.jar",
                    new FileInputStream(getTestBundle(BUNDLE_BASE_NAME + "-testA-1.0.jar")),
                    "digest1", null, null));
            this.waitForBundleEvents("Bundles must be installed and started.", listener,
                    new BundleEvent("osgi-installer-testA", "1.0", org.osgi.framework.BundleEvent.INSTALLED),
                    new BundleEvent("osgi-installer-testA", "1.0", org.osgi.framework.BundleEvent.STARTED));
            assertBundle("testA bundle added", "osgi-installer-testA", "1.0", Bundle.ACTIVE);
        }

        {
            final Object listener = this.startObservingBundleEvents();

            // Simulate later registration where some bundles have disappeared
            // the installer must mark them "not installable" and act accordingly
            final List<InstallableResource> r = new ArrayList<InstallableResource>();
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-needsB.jar")));
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testbundle-1.0.jar")));
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-snap.jar"), "digest1"));

            installer.registerResources(URL_SCHEME, r);
            this.waitForBundleEvents("Bundles must be installed and started.", listener,
                    new BundleEvent("osgi-installer-snapshot-test", "1.0.0.SNAPSHOT", org.osgi.framework.BundleEvent.INSTALLED),
                    new BundleEvent("osgi-installer-snapshot-test", "1.0.0.SNAPSHOT", org.osgi.framework.BundleEvent.STARTED),
                    new BundleEvent("osgi-installer-testB", org.osgi.framework.BundleEvent.STOPPED),
                    new BundleEvent("osgi-installer-testB", org.osgi.framework.BundleEvent.UNINSTALLED));

            assertBundle("Snapshot bundle must be started",
                    "osgi-installer-snapshot-test", "1.0.0.SNAPSHOT", Bundle.ACTIVE);
            assertNull("Bundle testB must be gone", findBundle("osgi-installer-testB"));
            final Bundle b = assertBundle("Bundle needsB must still be present",
                    "osgi-installer-needsB", "1.0", -1);
            final int state = b.getState();
            assertFalse("Bundle needsB must be stopped as testB is gone (" + state + ")", Bundle.ACTIVE == state);
            assertBundle("Testbundle must be back to 1.0 as 1.1 and 1.2 is gone",
                    "osgi-installer-testbundle", "1.0", Bundle.ACTIVE);
            assertBundle("testA bundle should still be present", "osgi-installer-testA", "1.0", Bundle.ACTIVE);
        }

        {
            final Object listener = this.startObservingBundleEvents();

            // Re-add the missing bundles and recheck
            installer.addResource(URL_SCHEME, getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testB-1.0.jar")));
            installer.addResource(URL_SCHEME, getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testbundle-1.2.jar")));

            this.waitForBundleEvents("Bundles must be installed and started.", listener,
                    new BundleEvent("osgi-installer-testbundle", org.osgi.framework.BundleEvent.UPDATED),
                    new BundleEvent("osgi-installer-testbundle", "1.2", org.osgi.framework.BundleEvent.STARTED),
                    new BundleEvent("osgi-installer-testB", "1.0", org.osgi.framework.BundleEvent.INSTALLED),
                    new BundleEvent("osgi-installer-testB", "1.0", org.osgi.framework.BundleEvent.STARTED));

            final String info = "After re-adding missing bundles";
            assertBundle(info, "osgi-installer-testB", "1.0", Bundle.ACTIVE);
            assertBundle(info, "osgi-installer-testbundle", "1.2", Bundle.ACTIVE);
            assertBundle(info, "osgi-installer-snapshot-test", "1.0.0.SNAPSHOT", Bundle.ACTIVE);

            final Bundle b = findBundle("osgi-installer-needsB");
            b.start();
            assertBundle("After reinstalling testB, needsB must be startable, ",
            		"osgi-installer-needsB", "1.0", Bundle.ACTIVE);
            assertBundle("testA bundle should still be present", "osgi-installer-testA", "1.0", Bundle.ACTIVE);
        }
    }

    @Test
    public void reAddZeroResourcesTest() throws Exception {
        {
            final Object listener = this.startObservingBundleEvents();
            final List<InstallableResource> r = new ArrayList<InstallableResource>();
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testB-1.0.jar")));
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-needsB.jar")));
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testbundle-1.0.jar")));
            r.add(getInstallableResource(getTestBundle(BUNDLE_BASE_NAME + "-testbundle-1.1.jar")));

            installer.registerResources(URL_SCHEME, r);
            this.waitForBundleEvents("Bundles must be installed and started.", listener,
                    new BundleEvent("osgi-installer-testB", "1.0", org.osgi.framework.BundleEvent.INSTALLED),
                    new BundleEvent("osgi-installer-testB", "1.0", org.osgi.framework.BundleEvent.STARTED),
                    new BundleEvent("osgi-installer-needsB", "1.0", org.osgi.framework.BundleEvent.INSTALLED),
                    new BundleEvent("osgi-installer-needsB", "1.0", org.osgi.framework.BundleEvent.STARTED),
                    new BundleEvent("osgi-installer-testbundle", "1.1", org.osgi.framework.BundleEvent.INSTALLED),
                    new BundleEvent("osgi-installer-testbundle", "1.1", org.osgi.framework.BundleEvent.STARTED));

            final String info = "After initial registration";
            assertBundle(info, "osgi-installer-testB", "1.0", Bundle.ACTIVE);
            assertBundle(info, "osgi-installer-needsB", "1.0", Bundle.ACTIVE);
            assertBundle(info, "osgi-installer-testbundle", "1.1", Bundle.ACTIVE);
        }

        {
            final Object listener = this.startObservingBundleEvents();

            installer.registerResources(URL_SCHEME, new LinkedList<InstallableResource>());
            this.waitForBundleEvents("Bundles must be installed and started.", listener,
                    new BundleEvent("osgi-installer-testB", "1.0", org.osgi.framework.BundleEvent.STOPPED),
                    new BundleEvent("osgi-installer-testB", "1.0", org.osgi.framework.BundleEvent.UNINSTALLED),
                    new BundleEvent("osgi-installer-needsB", "1.0", org.osgi.framework.BundleEvent.STOPPED),
                    new BundleEvent("osgi-installer-needsB", "1.0", org.osgi.framework.BundleEvent.UNINSTALLED),
                    new BundleEvent("osgi-installer-testbundle", "1.1", org.osgi.framework.BundleEvent.STOPPED),
                    new BundleEvent("osgi-installer-testbundle", "1.1", org.osgi.framework.BundleEvent.UNINSTALLED));
            assertNull("After registration with no resources, testB bundle must be gone", findBundle("osgi-installer-testB"));
            assertNull("After registration with no resources, testB bundle must be gone", findBundle("osgi-installer-needsB"));
            assertNull("After registration with no resources, testB bundle must be gone", findBundle("osgi-installer-testbundle"));
        }
    }
}
