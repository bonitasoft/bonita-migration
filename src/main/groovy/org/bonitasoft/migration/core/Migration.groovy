/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/

package org.bonitasoft.migration.core

/**
 * @author Baptiste Mesta
 */
class Migration {


    public static void main(String[] args) {
        new Migration().run()
    }

    public void run() {
        setupOutputs()
        execute()
    }

    private void setupOutputs() {
        def logInFile = new FileOutputStream(new File("migration-" + new Date().format("yyyy-MM-dd-HHmmss") + ".log"))
        System.setOut(new PrintStream(new SplitPrintStream(System.out, logInFile)))
        System.setErr(new PrintStream(new SplitPrintStream(System.err, logInFile)))
    }

    def execute(){
        println ''
        IOUtil.printInRectangle("", "Bonita BPM migration tool", "",
                "This tool will migrate your installation of Bonita BPM.",
                "Both database and bonita home will be modified.",
                "Please refer to the documentation for further steps to completely migrate your production environment.",
                "",
                "Warning:",
                "Back up the database AND the bonita home before migrating",
                "If you have a custom Look & Feel, test and update it, if it's necessary when the migration is finished.",
                "If you have customized the configuration of your bonita home, reapply the customizations when the migration is finished.", "")
        IOUtil.askIfWeContinue();
    }
}
