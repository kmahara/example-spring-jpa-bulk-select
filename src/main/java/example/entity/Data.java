package example.entity;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class Data implements Serializable {
  private static final long serialVersionUID = 1L;

  // bulk insert するために id は自動採番しない。
  @Id
  private Long id;

  @Column(nullable = false)
  private String value;
}
