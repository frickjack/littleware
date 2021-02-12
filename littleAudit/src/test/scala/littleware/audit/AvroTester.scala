package littleware.audit

import java.util.logging.Level
import org.junit.Assert._
import org.junit.Test
import org.apache.avro.io._
import org.apache.avro.generic.{ GenericDatumReader, GenericDatumWriter, GenericRecord }
import org.apache.kafka.clients.consumer._
import org.apache.kafka.clients.producer._

import littleware.test.LittleTest.log
import scala.util.Using

/**
 * Little tester of GetoptHelper
 */
class AvroTester extends littleware.scala.test.LittleTest {
    
    @Test
    def testMessPickler():Unit = try {
        val mess = TestMess("frickjack")
        val avroGeneric = TestMess.toAvroGeneric(mess)
        assertTrue(s"built an avro generic: ${mess.name} ?= ${avroGeneric.get("name")}", mess.name == avroGeneric.get("name"))
        val mess2 = TestMess.fromAvroGeneric(avroGeneric)
        assertTrue("pickle/unpickle is consistent", mess == mess2)
    } catch basicHandler

    /**
    * See the avro quick start at:
    *   http://avro.apache.org/docs/current/gettingstartedjava.html
    */
    @Test
    def testMessSerialization():Unit = try {
        val mess = TestMess("frickjack")
        val messBytes = AvroPickler.genericToBytes(TestMess.toAvroGeneric(mess))
        val mess2 = TestMess.fromAvroGeneric(AvroPickler.bytesToGeneric(messBytes, TestMess.schema))
        assertTrue(s"serialize/deserialize is consistent ${mess} ?= ${mess2}", mess == mess2)
    } catch basicHandler

    /**
     * Post and consume messages to/from kafka topic
     */
    @Test
    def testKafkaMess():Unit = try {
        val topicName = "audittest"
        val properties = new java.util.Properties()
        // Set the brokers (bootstrap servers)
        properties.setProperty("bootstrap.servers", "localhost:9092")
        // Set how to serialize key/value pairs
        properties.setProperty("key.serializer","org.apache.kafka.common.serialization.StringSerializer")
        properties.setProperty("value.serializer","org.apache.kafka.common.serialization.ByteArraySerializer")
        // Set the consumer group (all consumers must belong to a group).
        properties.setProperty("group.id", "UnitTests")
        // Set how to serialize key/value pairssssssssss
        properties.setProperty("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        properties.setProperty("value.deserializer","org.apache.kafka.common.serialization.ByteArrayDeserializer");
        // Point it to the brokers
        // When a group is first created, it has no offset stored to start reading from. This tells it to start
        // with the earliest record in the stream.
        properties.setProperty("auto.offset.reset","earliest");

        val producer:Producer[String,Array[Byte]] = new KafkaProducer[String, Array[Byte]](properties)
        val mess = TestMess("frickjack")
        val messBytes = AvroPickler.genericToBytes(TestMess.toAvroGeneric(mess))
        producer.send(new ProducerRecord(topicName, "1", messBytes))
        producer.flush()
        producer.close()

        val consumer:Consumer[String, Array[Byte]] = new KafkaConsumer[String, Array[Byte]](properties)
        // Subscribe to the 'test' topic
        consumer.subscribe(java.util.Collections.singletonList(topicName));

        val records:ConsumerRecords[String, Array[Byte]] = consumer.poll(1000)
        assertTrue("consumer got some records", records.count() > 0)
        
        records.forEach {
            record =>
            val mess2 = TestMess.fromAvroGeneric(AvroPickler.bytesToGeneric(record.value, TestMess.schema))

            log.log(Level.INFO, s"consumer got a message: ${mess2}")
            assertTrue(s"received equals sent: ${mess2} =? ${mess}", mess2 == mess)
        }
    } catch basicHandler
}
