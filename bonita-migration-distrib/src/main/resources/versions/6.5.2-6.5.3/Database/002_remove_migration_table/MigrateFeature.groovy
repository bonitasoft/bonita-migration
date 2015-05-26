def shouldDropTable = false
try {
    sql.firstRow("SELECT * FROM migration_plan WHERE id=1").size()
    shouldDropTable = true
} catch (Exception e) {
    // the table do not exists do nothing
    println("The table migration_plan is not here")
}
if (shouldDropTable) {
    println("The table migration_plan is present, it will be dropped")
    sql.execute("DROP TABLE migration_plan")
    sql.execute("DELETE FROM sequence WHERE id = 10100")
}
