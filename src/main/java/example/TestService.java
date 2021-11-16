package example;

import java.nio.CharBuffer;
import java.util.Iterator;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import example.entity.Data;
import example.repository.DataRepository;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TestService {
  private static final long DATA_SIZE = 1_000_000;
  private static final int VALUE_LENGTH = 255;
  private static final int BATCH_SIZE = 10000;
  private static final int FETCH_SIZE = 10000;

  @PersistenceContext
  private EntityManager entityManager;

  @Autowired
  private DataRepository dataRepository;

  /**
   * DB に DATA_SIZE で指定した件数のデータを作成する。<br>
   *
   * すでに作成済みの場合は何もしない。 <br>
   * INSERT 途中で中断した場合は再実行時にそこから再開する。
   */
  @Transactional
  public void init() {
    long count = dataRepository.count();

    if (count == DATA_SIZE) {
      return;
    }

    log.info("create datas");

    // 一定の長さの文字列を生成。\0 で初期化されている。
    String value = CharBuffer.allocate(VALUE_LENGTH).toString();

    int addCount = 0;

    // このセッション限定でバッチサイズを明示的に指定する。
    // 複数の insert が１つにまとめて実行される。
    entityManager.unwrap(Session.class).setJdbcBatchSize(BATCH_SIZE);

    for (long id = count; id < DATA_SIZE; id++) {
      Data data = new Data();
      data.setId(id);
      data.setValue(value);

      // コミット対象に追加。
      entityManager.persist(data);

      addCount++;

      // コミット対象が一定数溜まったら flush で実際の INSERT 文を実行し、
      // clear でエンティティを entityManager の管理対象から外す。エンティティが GC 対象になる。
      if (addCount == BATCH_SIZE) {
        log.info("flush: id: " + id);
        entityManager.flush();
        entityManager.clear();

        addCount = 0;
      }
    }

    log.info("create datas: end");
  }

  /**
   * -Xmx128m を指定してプログラムを実行すると、 dataRepository.findAll() 時に OutOfMemoryError となる。
   *
   * @throws Exception
   */
  @Transactional
  public void test1() throws Exception {
    log.info("test1");

    Iterator<Data> iterator = dataRepository.findAll().iterator();

    int count = 0;

    try {
      while (iterator.hasNext()) {
        count++;

        if (count % 10000 == 0) {
          log.info("count: " + count);
        }

        @SuppressWarnings("unused")
        Data data = iterator.next();
      }
    } catch (Exception e) {
      throw new Exception("count = " + count, e);
    }
  }

  private int selectCount = 0;

  /**
   * fetch size を指定し stream で処理する改善版。
   *
   * @throws Exception
   */
  @Transactional(readOnly = true)
  public void test2() throws Exception {
    log.info("test2");

    selectCount = 1;

    try {
      entityManager.createQuery("from Data", Data.class)
          .setHint(org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE, FETCH_SIZE).getResultStream()
          .forEach(data -> {
            selectCount++;

            if (selectCount % 10000 == 0) {
              log.info("count: " + selectCount);
            }

            // ここをコメントアウトするとエンティティは entityManager に紐付けたままになり、OutOfMemoryError となる。
            entityManager.detach(data);
          });

    } catch (Exception e) {
      throw new Exception("count = " + selectCount, e);
    }
  }
}
