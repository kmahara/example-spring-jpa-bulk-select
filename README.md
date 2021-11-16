# 概要

Spring Data を使用した bulk insert, bulk select の例。

# 実行手順

* mysql サーバを起動する。

```
docker-compose up -d
```

* example.Application を実行する。

```
gradle bootRun
```

  * Eclipse 等で実行する場合、起動オプションで最大メモリを指定する: `-Xmx128m`

# Bulk insert 時に気をつけること

* JDBC に対し batch size を指定する。
* トランザクション内で entityManager.persist() を行い、entityManager に insert 対象として追加する。
* batch size 分のデータが溜まったら entityManager.flush() を実行する。
  * まだ DB に対して SQL 発行していないエンティティが複数にまとめられた INSERT 文が作成され、DB に対し実行される。
* 続けて entityManager.clear() を呼び、エンティティを entityManager の管理対象から外し GC 時にメモリから開放できるようにする。

# Bulk select 時に気をつけること

* `@Transactional(readOnly = true)` を指定し、read only のトランザクション内で実行する。
* select 時に org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE で一度に取得するデータ量を指定する。
* 戻り値は stream で受け取る。
* stream.forEach() 内で目的の処理を実行したあと、entityManager.detach() でエンティティを管理対象から外す。

# 参考

* https://www.baeldung.com/spring-data-java-8