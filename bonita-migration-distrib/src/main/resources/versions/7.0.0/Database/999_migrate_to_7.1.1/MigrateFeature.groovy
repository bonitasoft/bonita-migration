reporter.addWarning("""Caution: The 6.5.4 release contains fixes to certain bugs that were found in early 7.x.y versions, up to 7.1.0. When you are ready to migrate from 6.5.4 to a 7.x.y version, make sure you migrate to at least 7.1.1 to be sure that these fixes are present in your new version after migration. To migrate form 6.5.4 to 7.1.1 or later there are two phases: first you upgrade to 7.0.0 then to the latest version. You are recommended not to start 7.0.0 after you migrate to it, but to proceed immediately with the second phase of the migration.
 * [BS-13249] - IE loses input field focus if the load image is clicked during task loading
 * [BS-13530] - In a cluster, Portal task view shows some Done tasks with null value instead of process name
 * [BS-13815] - Clicking immediately on Task while loading leads to an error message
 * [BS-13975] - Next button remains visible on the loading page in a pageflow
""")