package io.kafbat.ui.serdes;

import io.kafbat.ui.exception.ValidationException;
import io.kafbat.ui.serde.api.Serde;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;

@RequiredArgsConstructor
public class ProducerRecordCreator {

  private final Serde.Serializer keySerializer;
  private final Serde.Serializer valuesSerializer;

  public ProducerRecord<byte[], byte[]> create(String topic,
                                               @Nullable Integer partition,
                                               @Nullable String key,
                                               @Nullable String value,
                                               @Nullable Map<String, Object> headers) {
    return new ProducerRecord<>(
        topic,
        partition,
        key == null ? null : keySerializer.serialize(key),
        value == null ? null : valuesSerializer.serialize(value),
        headers == null ? null : createHeaders(headers)
    );
  }

  private Iterable<Header> createHeaders(Map<String, Object> clientHeaders) {
    RecordHeaders headers = new RecordHeaders();
    clientHeaders.forEach((k, v) -> {
      if (v instanceof List<?> valueList) {
        valueList.forEach(value -> headers.add(new RecordHeader(k, valueToBytes(value))));
      } else {
        headers.add(new RecordHeader(k, valueToBytes(v)));
      }
    });
    return headers;
  }

  private byte[] valueToBytes(Object value) {
    if (value instanceof List<?> || value instanceof Map<?, ?>) {
      throw new ValidationException("Header values can only be string or list of strings");
    }
    return value != null ? String.valueOf(value).getBytes() : null;
  }

}
