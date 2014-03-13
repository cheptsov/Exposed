package kotlinx.nosql

import java.util.ArrayList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.DateTime

abstract class Schema(val name: String) {
    // TODO TODO TODO
    // val columns = ArrayList<AbstractColumn<*, *, *>>()

    // TODO TODO TODO
    class object {
        val threadLocale = ThreadLocal<Schema>()

        fun <T: Schema> current(): T {
            return threadLocale.get() as T
        }

        fun set(schema: Schema) {
            return threadLocale.set(schema)
        }
    }
}

abstract class KeyValueSchema(name: String): Schema(name) {
}

abstract class AbstractTableSchema(name: String): Schema(name) {
}

class Id<P, R: TableSchema<P>>(val value: P) {
    override fun toString() = value.toString()

    override fun equals(other: Any?): Boolean {
        return if (other is Id<*, *>) value.equals((other as Id<*, *>).value) else false
    }
    override fun hashCode(): Int {
        return value.hashCode()
    }
}

abstract class TableSchema<P>(tableName: String, primaryKey: AbstractColumn<P, out TableSchema<P>, P>): AbstractTableSchema(tableName) {
    val pk = AbstractColumn<Id<P, TableSchema<P>>, TableSchema<P>, P>(primaryKey.name, primaryKey.valueClass, ColumnType.PRIMARY_ID)
}

open class PrimaryKey<P>(val name: String, val javaClass: Class<P>, val columnType: ColumnType) {
    class object {
        fun string(name: String) = PrimaryKey<String>(name, javaClass<String>(), ColumnType.STRING)
        fun integer(name: String) = PrimaryKey<Int>(name, javaClass<Int>(), ColumnType.INTEGER)
    }
}

val <C, T : TableSchema<C>> T.ID: AbstractColumn<Id<C, T>, T, C>
    get () {
        return pk as AbstractColumn<Id<C, T>, T, C>
    }

class Discriminator<V, T: DocumentSchema<out Any, out Any>>(val column: AbstractColumn<V, T, V>, val value: V) {
}

abstract class DocumentSchema<P, V>(name: String, val valueClass: Class<V>, primaryKey: AbstractColumn<P,
        out DocumentSchema<P, V>, P>, val discriminator: Discriminator<out Any, out DocumentSchema<P, V>>? = null) : TableSchema<P>(name, primaryKey) {
    {
        if (discriminator != null) {
            val emptyDiscriminators = CopyOnWriteArrayList<Discriminator<*, *>>()
            val discriminators = tableDiscriminators.putIfAbsent(name, emptyDiscriminators)
            if (discriminators != null)
                discriminators.add(discriminator)
            else
                emptyDiscriminators.add(discriminator)
            // TODO TODO TODO
            discriminatorClasses.put(discriminator, this.valueClass)
            discriminatorSchemaClasses.put(discriminator, this.javaClass)
            discriminatorSchemas.put(discriminator, this)
        }
    }

    class object {
        val tableDiscriminators = ConcurrentHashMap<String, MutableList<Discriminator<*, *>>>()
        val discriminatorClasses = ConcurrentHashMap<Discriminator<*, *>, Class<*>>()
        val discriminatorSchemaClasses = ConcurrentHashMap<Discriminator<*, *>, Class<*>>()
        val discriminatorSchemas = ConcurrentHashMap<Discriminator<*, *>, Schema>()
    }
}

fun <T: TableSchema<P>, R: TableSchema<P>, P> id(name: String, schema: R): AbstractColumn<Id<P, R>, T, P> = AbstractColumn(name, schema.ID.valueClass, ColumnType.FOREIGN_ID)
fun <T: TableSchema<P>, R: TableSchema<P>, P> T.id(name: String, schema: R): AbstractColumn<Id<P, R>, T, P> = AbstractColumn(name, schema.ID.valueClass, ColumnType.FOREIGN_ID)

fun <T: TableSchema<P>, R: TableSchema<P>, P> nullableId(name: String, schema: R): NullableIdColumn<P, T, R> = NullableIdColumn(name, schema.ID.valueClass, ColumnType.FOREIGN_ID)
fun <T: TableSchema<P>, R: TableSchema<P>, P> T.nullableId(name: String, schema: R): NullableIdColumn<P, T, R> = NullableIdColumn(name, schema.ID.valueClass, ColumnType.FOREIGN_ID)

fun <T: Schema> string(name: String): AbstractColumn<String, T, String> = AbstractColumn(name, javaClass<String>(), ColumnType.STRING)
fun <T: Schema> T.string(name: String): AbstractColumn<String, T, String> = AbstractColumn(name, javaClass<String>(), ColumnType.STRING)

fun <T: Schema> boolean(name: String): AbstractColumn<Boolean, T, Boolean> = AbstractColumn(name, javaClass<Boolean>(), ColumnType.BOOLEAN)
fun <T: Schema> T.boolean(name: String): AbstractColumn<Boolean, T, Boolean> = AbstractColumn(name, javaClass<Boolean>(), ColumnType.BOOLEAN)

fun <T: Schema> date(name: String): AbstractColumn<LocalDate, T, LocalDate> = AbstractColumn(name, javaClass<LocalDate>(), ColumnType.DATE)
fun <T: Schema> T.date(name: String): AbstractColumn<LocalDate, T, LocalDate> = AbstractColumn(name, javaClass<LocalDate>(), ColumnType.DATE)

fun <T: Schema> time(name: String): AbstractColumn<LocalTime, T, LocalTime> = AbstractColumn(name, javaClass<LocalTime>(), ColumnType.TIME)
fun <T: Schema> T.time(name: String): AbstractColumn<LocalTime, T, LocalTime> = AbstractColumn(name, javaClass<LocalTime>(), ColumnType.TIME)

fun <T: Schema> dateTime(name: String): AbstractColumn<DateTime, T, DateTime> = AbstractColumn(name, javaClass<DateTime>(), ColumnType.DATE_TIME)
fun <T: Schema> T.dateTime(name: String): AbstractColumn<DateTime, T, DateTime> = AbstractColumn(name, javaClass<DateTime>(), ColumnType.DATE_TIME)

fun <T: Schema> double(name: String): AbstractColumn<Double, T, Double> = AbstractColumn(name, javaClass<Double>(), ColumnType.DOUBLE)
fun <T: Schema> T.double(name: String): AbstractColumn<Double, T, Double> = AbstractColumn(name, javaClass<Double>(), ColumnType.DOUBLE)

fun <T: Schema> integer(name: String): AbstractColumn<Int, T, Int> = AbstractColumn(name, javaClass<Int>(), ColumnType.INTEGER)
fun <T: Schema> T.integer(name: String): AbstractColumn<Int, T, Int> = AbstractColumn(name, javaClass<Int>(), ColumnType.INTEGER)

fun <T: Schema> float(name: String): AbstractColumn<Float, T, Float> = AbstractColumn(name, javaClass<Float>(), ColumnType.FLOAT)
fun <T: Schema> T.float(name: String): AbstractColumn<Float, T, Float> = AbstractColumn(name, javaClass<Float>(), ColumnType.FLOAT)

fun <T: Schema> long(name: String): AbstractColumn<Long, T, Long> = AbstractColumn(name, javaClass<Long>(), ColumnType.LONG)
fun <T: Schema> T.long(name: String): AbstractColumn<Long, T, Long> = AbstractColumn(name, javaClass<Long>(), ColumnType.LONG)

fun <T: Schema> short(name: String): AbstractColumn<Short, T, Short> = AbstractColumn(name, javaClass<Short>(), ColumnType.SHORT)
fun <T: Schema> T.short(name: String): AbstractColumn<Short, T, Short> = AbstractColumn(name, javaClass<Short>(), ColumnType.SHORT)

fun <T: Schema> byte(name: String): AbstractColumn<Byte, T, Byte> = AbstractColumn(name, javaClass<Byte>(), ColumnType.BYTE)
fun <T: Schema> T.byte(name: String): AbstractColumn<Byte, T, Byte> = AbstractColumn(name, javaClass<Byte>(), ColumnType.BYTE)

fun <T: Schema> nullableString(name: String): NullableColumn<String, T> = NullableColumn(name, javaClass<String>(), ColumnType.STRING)
fun <T: Schema> T.nullableString(name: String): NullableColumn<String, T> = NullableColumn(name, javaClass<String>(), ColumnType.STRING)

fun <T: Schema> nullableInteger(name: String): NullableColumn<Int, T> = NullableColumn(name, javaClass<Int>(), ColumnType.INTEGER)
fun <T: Schema> T.nullableInteger(name: String): NullableColumn<Int, T> = NullableColumn(name, javaClass<Int>(), ColumnType.INTEGER)

fun <T: Schema> nullableBoolean(name: String): NullableColumn<Boolean, T> = NullableColumn(name, javaClass<Boolean>(), ColumnType.BOOLEAN)
fun <T: Schema> T.nullableBoolean(name: String): NullableColumn<Boolean, T> = NullableColumn(name, javaClass<Boolean>(), ColumnType.BOOLEAN)

fun <T: Schema> nullableDate(name: String): NullableColumn<LocalDate, T> = NullableColumn(name, javaClass<LocalDate>(), ColumnType.DATE)
fun <T: Schema> T.nullableDate(name: String): NullableColumn<LocalDate, T> = NullableColumn(name, javaClass<LocalDate>(), ColumnType.DATE)

fun <T: Schema> nullableTime(name: String): NullableColumn<LocalTime, T> = NullableColumn(name, javaClass<LocalTime>(), ColumnType.TIME)
fun <T: Schema> T.nullableTime(name: String): NullableColumn<LocalTime, T> = NullableColumn(name, javaClass<LocalTime>(), ColumnType.TIME)

fun <T: Schema> nullableDateTime(name: String): NullableColumn<DateTime, T> = NullableColumn(name, javaClass<DateTime>(), ColumnType.DATE_TIME)
fun <T: Schema> T.nullableDateTime(name: String): NullableColumn<DateTime, T> = NullableColumn(name, javaClass<DateTime>(), ColumnType.DATE_TIME)

fun <T: Schema> nullableDouble(name: String): NullableColumn<Double, T> = NullableColumn(name, javaClass<Double>(), ColumnType.DOUBLE)
fun <T: Schema> T.nullableDouble(name: String): NullableColumn<Double, T> = NullableColumn(name, javaClass<Double>(), ColumnType.DOUBLE)

fun <T: Schema> nullableFloat(name: String): NullableColumn<Float, T> = NullableColumn(name, javaClass<Float>(), ColumnType.FLOAT)
fun <T: Schema> T.nullableFloat(name: String): NullableColumn<Float, T> = NullableColumn(name, javaClass<Float>(), ColumnType.FLOAT)

fun <T: Schema> nullableLong(name: String): NullableColumn<Long, T> = NullableColumn(name, javaClass<Long>(), ColumnType.LONG)
fun <T: Schema> T.nullableLong(name: String): NullableColumn<Long, T> = NullableColumn(name, javaClass<Long>(), ColumnType.LONG)

fun <T: Schema> nullableShort(name: String): NullableColumn<Short, T> = NullableColumn(name, javaClass<Short>(), ColumnType.SHORT)
fun <T: Schema> T.nullableShort(name: String): NullableColumn<Short, T> = NullableColumn(name, javaClass<Short>(), ColumnType.SHORT)

fun <T: Schema> nullableByte(name: String): NullableColumn<Byte, T> = NullableColumn(name, javaClass<Byte>(), ColumnType.BYTE)
fun <T: Schema> T.nullableByte(name: String): NullableColumn<Byte, T> = NullableColumn(name, javaClass<Byte>(), ColumnType.BYTE)

//fun <T: AbstractSchema, C> T.setColumn(name: String, javaClass: Class<C>): SetColumn<C, T> = SetColumn(name, javaClass)

fun <T: Schema> setOfString(name: String): AbstractColumn<Set<String>, T, String> = AbstractColumn<Set<String>, T, String>(name, javaClass(), ColumnType.STRING_SET)

fun <T: Schema> T.setOfString(name: String): AbstractColumn<Set<String>, T, String> = AbstractColumn<Set<String>, T, String>(name, javaClass<String>(), ColumnType.STRING_SET)

fun <T: Schema> T.setOfInteger(name: String): AbstractColumn<Set<Int>, T, Int> = AbstractColumn<Set<Int>, T, Int>(name, javaClass<Int>(), ColumnType.INTEGER_SET)

//fun <T: AbstractSchema, C> T.listColumn(name: String, javaClass: Class<C>): ListColumn<C, T> = ListColumn(name, javaClass)

fun <T: Schema> listOfString(name: String): AbstractColumn<List<String>, T, String> = AbstractColumn<List<String>, T, String>(name, javaClass<String>(), ColumnType.STRING_LIST)

fun <T: Schema> T.listOfString(name: String): AbstractColumn<List<String>, T, String> = AbstractColumn<List<String>, T, String>(name, javaClass<String>(), ColumnType.STRING_LIST)

fun <T: Schema> T.listOfInteger(name: String): AbstractColumn<List<Int>, T, Int> = AbstractColumn<List<Int>, T, Int>(name, javaClass<Int>(), ColumnType.INTEGER_LIST)

fun <T: AbstractTableSchema> T.delete(body: T.() -> Op) {
    FilterQuery(this, body()) delete { }
}

// TODO TODO TODO
fun <T: AbstractTableSchema, X> T.columns(selector: T.() -> X): X {
    Schema.set(this)
    return selector();
}

fun <T: AbstractTableSchema, B> FilterQuery<T>.map(statement: T.(Map<Any, Any>) -> B): List<B> {
    val results = ArrayList<B>()
    //Query
    return results
}

class Template1<T: AbstractTableSchema, A>(val table: T, val a: AbstractColumn<A, T, *>) {
    fun invoke(av: A): Array<Pair<AbstractColumn<*, T, *>, *>> {
        return array(Pair(a, av))
    }
}

class Quadruple<A1, A2, A3, A4>(val a1: A1, val a2: A2, val a3: A3, val a4: A4) {
    public fun component1(): A1 = a1
    public fun component2(): A2 = a2
    public fun component3(): A3 = a3
    public fun component4(): A4 = a4
}

class Quintuple<A1, A2, A3, A4, A5>(val a1: A1, val a2: A2, val a3: A3, val a4: A4, val a5: A5) {
    public fun component1(): A1 = a1
    public fun component2(): A2 = a2
    public fun component3(): A3 = a3
    public fun component4(): A4 = a4
    public fun component5(): A5 = a5
}

class Sextuple<A1, A2, A3, A4, A5, A6>(val a1: A1, val a2: A2, val a3: A3, val a4: A4, val a5: A5, val a6: A6) {
    public fun component1(): A1 = a1
    public fun component2(): A2 = a2
    public fun component3(): A3 = a3
    public fun component4(): A4 = a4
    public fun component5(): A5 = a5
    public fun component6(): A6 = a6
}

class Septuple<A1, A2, A3, A4, A5, A6, A7>(val a1: A1, val a2: A2, val a3: A3, val a4: A4, val a5: A5, val a6: A6, val a7: A7) {
    public fun component1(): A1 = a1
    public fun component2(): A2 = a2
    public fun component3(): A3 = a3
    public fun component4(): A4 = a4
    public fun component5(): A5 = a5
    public fun component6(): A6 = a6
    public fun component7(): A7 = a7
}

class Octuple<A1, A2, A3, A4, A5, A6, A7, A8>(val a1: A1, val a2: A2, val a3: A3, val a4: A4, val a5: A5, val a6: A6, val a7: A7, val a8: A8) {
    public fun component1(): A1 = a1
    public fun component2(): A2 = a2
    public fun component3(): A3 = a3
    public fun component4(): A4 = a4
    public fun component5(): A5 = a5
    public fun component6(): A6 = a6
    public fun component7(): A7 = a7
    public fun component8(): A8 = a8
}

class Nonuple<A1, A2, A3, A4, A5, A6, A7, A8, A9>(val a1: A1, val a2: A2, val a3: A3, val a4: A4, val a5: A5, val a6: A6, val a7: A7, val a8: A8, val a9: A9) {
    public fun component1(): A1 = a1
    public fun component2(): A2 = a2
    public fun component3(): A3 = a3
    public fun component4(): A4 = a4
    public fun component5(): A5 = a5
    public fun component6(): A6 = a6
    public fun component7(): A7 = a7
    public fun component8(): A8 = a8
    public fun component9(): A9 = a9
}

class Decuple<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10>(val a1: A1, val a2: A2, val a3: A3, val a4: A4, val a5: A5, val a6: A6, val a7: A7, val a8: A8, val a9: A9, val a10: A10) {
    public fun component1(): A1 = a1
    public fun component2(): A2 = a2
    public fun component3(): A3 = a3
    public fun component4(): A4 = a4
    public fun component5(): A5 = a5
    public fun component6(): A6 = a6
    public fun component7(): A7 = a7
    public fun component8(): A8 = a8
    public fun component9(): A9 = a9
    public fun component10(): A10 = a10
}

fun <T: AbstractTableSchema, A, B> T.template(a: AbstractColumn<A, T, *>, b: AbstractColumn<B, T, *>): Template2<T, A, B> {
    return Template2(a, b)
}

class Template2<T: Schema, A, B>(val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>) {
    fun <C> plus(c: AbstractColumn<C, T, *>): Template3<T, A, B, C> {
        return Template3(a, b, c)
    }
}

class Template3<T: Schema, A, B, C>(val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>, val c: AbstractColumn<C, T, *>) {
    fun <D> plus(d: AbstractColumn<D, T, *>): Template4<T, A, B, C, D> {
        return Template4(a, b, c, d)
    }
}

/*fun <T : TableSchema<P>, P, A, B> Template2<T, A, B>.insert(statement: () -> Triple<P, A, B>) {
    val tt = statement()
    Session.current().insert(array(Pair(Schema.current<T>().ID, tt.component1()), Pair(a, tt.component2()), Pair(b, tt.component3())))
}

fun <T : TableSchema<P>, P, C> AbstractColumn<C, T, *>.insert(statement: () -> Pair<P, C>) {
    val tt = statement()
    val id: AbstractColumn<P, T, *> = Schema.current<T>().ID // Type inference failure
    Session.current().insert(array(Pair(id, tt.component1()), Pair(this, tt.component2())))
}*/

class Template4<T: Schema, A, B, C, D>(val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>, val c: AbstractColumn<C, T, *>, val d: AbstractColumn<D, T, *>) {
    fun <E> plus(e: AbstractColumn<E, T, *>): Template5<T, A, B, C, D, E> {
        return Template5(a, b, c, d, e)
    }

    fun insert(statement: () -> Quadruple<A, B, C, D>) {
        val tt = statement()
        Session.current().insert(array(Pair(a, tt.component1()), Pair(b, tt.component2()), Pair(c, tt.component3()), Pair(d, tt.component4())))
    }
}

class Template5<T: Schema, A, B, C, D, E>(val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>,
                                          val c: AbstractColumn<C, T, *>, val d: AbstractColumn<D, T, *>,
                                          val e: AbstractColumn<E, T, *>) {
    fun <F> plus(f: AbstractColumn<F, T, *>): Template6<T, A, B, C, D, E, F> {
        return Template6(a, b, c, d, e, f)
    }
}

class Template6<T: Schema, A, B, C, D, E, F>(val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>,
                                          val c: AbstractColumn<C, T, *>, val d: AbstractColumn<D, T, *>,
                                          val e: AbstractColumn<E, T, *>, val f: AbstractColumn<F, T, *>) {
    fun <G> plus(g: AbstractColumn<G, T, *>): Template7<T, A, B, C, D, E, F, G> {
        return Template7(a, b, c, d, e, f, g)
    }
}

class Template7<T: Schema, A, B, C, D, E, F, G>(val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>,
                                             val c: AbstractColumn<C, T, *>, val d: AbstractColumn<D, T, *>,
                                             val e: AbstractColumn<E, T, *>, val f: AbstractColumn<F, T, *>,
                                             val g: AbstractColumn<G, T, *>) {
    fun <H> plus(h: AbstractColumn<H, T, *>): Template8<T, A, B, C, D, E, F, G, H> {
        return Template8(a, b, c, d, e, f, g, h)
    }
}

class Template8<T: Schema, A, B, C, D, E, F, G, H>(val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>,
                                                val c: AbstractColumn<C, T, *>, val d: AbstractColumn<D, T, *>,
                                                val e: AbstractColumn<E, T, *>, val f: AbstractColumn<F, T, *>,
                                                val g: AbstractColumn<G, T, *>, val h: AbstractColumn<H, T, *>) {
    fun <J> plus(j: AbstractColumn<J, T, *>): Template9<T, A, B, C, D, E, F, G, H, J> {
        return Template9(a, b, c, d, e, f, g, h, j)
    }
}

class Template9<T: Schema, A, B, C, D, E, F, G, H, J>(val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>,
                                                   val c: AbstractColumn<C, T, *>, val d: AbstractColumn<D, T, *>,
                                                   val e: AbstractColumn<E, T, *>, val f: AbstractColumn<F, T, *>,
                                                   val g: AbstractColumn<G, T, *>, val h: AbstractColumn<H, T, *>,
                                                   val j: AbstractColumn<J, T, *>) {
    fun <K> plus(k: AbstractColumn<K, T, *>): Template10<T, A, B, C, D, E, F, G, H, J, K> {
        return Template10(a, b, c, d, e, f, g, h, j, k)
    }
}

class Template10<T: Schema, A, B, C, D, E, F, G, H, J, K>(val a: AbstractColumn<A, T, *>, val b: AbstractColumn<B, T, *>,
                                                      val c: AbstractColumn<C, T, *>, val d: AbstractColumn<D, T, *>,
                                                      val e: AbstractColumn<E, T, *>, val f: AbstractColumn<F, T, *>,
                                                      val g: AbstractColumn<G, T, *>, val h: AbstractColumn<H, T, *>,
                                                      val j: AbstractColumn<J, T, *>, val k: AbstractColumn<K, T, *>) {

}