package org.apache.maven.it;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;
import java.util.Properties;

/**
 * 
 * @author Benjamin Bentmann
 * @version $Id$
 */
public class MavenITmng1999Test
    extends AbstractMavenIntegrationTestCase
{

    public MavenITmng1999Test()
    {
        super( "(2.9999,)" );
    }

    /**
     * Test that default reports can be suppressed via inheritance from the parent.
     */
    public void testitMNG1999()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-1999" );

        Verifier verifier = new Verifier( new File( testDir, "child" ).getAbsolutePath() );
        verifier.deleteDirectory( "target" );
        verifier.setAutoclean( false );
        verifier.executeGoal( "validate" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        Properties props = verifier.loadProperties( "target/reports.properties" );
        assertEquals( "0", props.getProperty( "reports" ) );
        assertNull( props.getProperty( "reports.0" ) );
    }

}