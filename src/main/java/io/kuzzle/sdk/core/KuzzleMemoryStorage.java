package io.kuzzle.sdk.core;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import io.kuzzle.sdk.listeners.KuzzleResponseListener;
import io.kuzzle.sdk.listeners.OnQueryDoneListener;
import io.kuzzle.sdk.util.KuzzleJSONObject;
import io.kuzzle.sdk.util.memoryStorage.Action;
import io.kuzzle.sdk.util.memoryStorage.BitOP;
import io.kuzzle.sdk.util.memoryStorage.KuzzleMemoryStorageCommands;
import io.kuzzle.sdk.util.memoryStorage.ObjectCommand;
import io.kuzzle.sdk.util.memoryStorage.Position;
import io.kuzzle.sdk.util.memoryStorage.SetParams;
import io.kuzzle.sdk.util.memoryStorage.ZParams;

/**
 * Kuzzle's memory storage is a separate data store from the database layer.
 * It is internaly based on Redis. You can access most of Redis functions (all
 * lowercased), excepting:
 *   * all cluster based functions
 *   * all script based functions
 *   * all cursors functions
 *
 */

public class KuzzleMemoryStorage implements KuzzleMemoryStorageCommands {

  private Kuzzle  kuzzle;
  private Kuzzle.QueryArgs  queryArgs = new Kuzzle.QueryArgs();
  private KuzzleResponseListener<JSONObject> listener;
  private KuzzleOptions options;

  public KuzzleMemoryStorage(@NonNull final Kuzzle kuzzle) {
    this.kuzzle = kuzzle;
  }

  protected KuzzleMemoryStorage send(@NonNull final Action action) {
    return send(action, null);
  }

  protected KuzzleMemoryStorage send(@NonNull final Action action, final KuzzleJSONObject query) {
    queryArgs.controller = "ms";
    queryArgs.action = action.toString();
    try {
      kuzzle.query(queryArgs, (query == null ? new KuzzleJSONObject() : query), options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject response) {
          if (listener != null) {
            listener.onSuccess(response);
          }
        }

        @Override
        public void onError(JSONObject error) {
          if (listener != null) {
            listener.onError(error);
          }
        }
      });
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public KuzzleMemoryStorage setListener(final KuzzleResponseListener<JSONObject> listener) {
    this.listener = listener;
    return this;
  }

  public KuzzleMemoryStorage setOptions(final KuzzleOptions options) {
    this.options = options;
    return this;
  }

  @Override
  public KuzzleMemoryStorage append(final String key, final String value) {
    return send(Action.append, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", value));
  }

  @Override
  public KuzzleMemoryStorage bgrewriteaof() {
    return send(Action.bgrewriteaof);
  }

  @Override
  public KuzzleMemoryStorage bgsave() {
    return send(Action.bgsave);
  }

  @Override
  public KuzzleMemoryStorage bitcount(final String key) {
    return send(Action.bitcount, new KuzzleJSONObject().put("_id", key));
  }

  @Override
  public KuzzleMemoryStorage bitcount(final String key, final long start, final long end) {
      return send(Action.bitcount, new KuzzleJSONObject()
          .put("_id", key)
          .put("body", new KuzzleJSONObject()
            .put("start", start)
            .put("end", end)));
  }

  @Override
  public KuzzleMemoryStorage bitop(final BitOP op, final String destKey, final String... srcKeys) {
    return send(Action.bitop, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
          .put("operation", op.toString())
          .put("destKey", destKey)
          .put("keys", srcKeys)));
  }

  @Override
  public KuzzleMemoryStorage bitpos(final String id, final long bit) {
    return bitpos(new KuzzleJSONObject()
        .put("_id", id)
        .put("body", new KuzzleJSONObject()
          .put("bit", bit)));
  }

  @Override
  public KuzzleMemoryStorage bitpos(final String id, final long bit, final long start) {
    return bitpos(new KuzzleJSONObject()
        .put("_id", id)
        .put("body", new KuzzleJSONObject()
          .put("bit", bit)
          .put("start", start)));
  }

  @Override
  public KuzzleMemoryStorage bitpos(final String id, final long bit, final long start, final long end) {
    return bitpos(new KuzzleJSONObject()
        .put("_id", id)
        .put("body", new KuzzleJSONObject()
          .put("bit", bit)
          .put("start", start)
          .put("end", end)));
  }

  private KuzzleMemoryStorage bitpos(final KuzzleJSONObject query) {
    return send(Action.bitpos, query);
  }

  @Override
  public KuzzleMemoryStorage blpop(final String[] args, long timeout) {
    return send(Action.blpop, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
          .put("src", args)
          .put("timeout", timeout)));
  }

  @Override
  public KuzzleMemoryStorage brpoplpush(final String source, final String destination, final int timeout) {
    return send(Action.brpoplpush, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
          .put("source", source)
          .put("destination", destination)
          .put("timeout", timeout)));
  }

  @Override
  public KuzzleMemoryStorage dbsize() {
    return send(Action.dbsize);
  }

  @Override
  public KuzzleMemoryStorage decrby(final String key, final long integer) {
    return send(Action.decrby, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
          .put("value", integer)));
  }

  @Override
  public KuzzleMemoryStorage discard() {
    return send(Action.discard);
  }

  @Override
  public KuzzleMemoryStorage  exec() {
    return send(Action.exec);
  }

  @Override
  public KuzzleMemoryStorage expire(final String key, int seconds) {
    return send(Action.expire, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
          .put("seconds", seconds)));
  }

  @Override
  public KuzzleMemoryStorage expireat(final String key, final long timestamp) {
    return send(Action.expireat, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
          .put("timestamp", timestamp)));
  }

  @Override
  public KuzzleMemoryStorage flushdb() {
    return send(Action.flushdb);
  }

  @Override
  public KuzzleMemoryStorage getbit(final String key, final long offset) {
    return send(Action.getbit, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("offset", offset)));
  }

  @Override
  public KuzzleMemoryStorage getrange(final String key, final long startOffset, final long endOffset) {
    return send(Action.getrange, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("start", startOffset)
            .put("end", endOffset)));
  }

  @Override
  public KuzzleMemoryStorage hdel(final String key, final String... fields) {
    KuzzleJSONObject query = new KuzzleJSONObject();
    query.put("_id", key);
    query.put("body", new KuzzleJSONObject()
        .put("fields", fields));
    return send(Action.hdel, query);
  }

  @Override
  public KuzzleMemoryStorage hexists(final String key, final String field) {
    return send(Action.hexists, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("field", field)));
  }

  @Override
  public KuzzleMemoryStorage hincrby(final String key, final String field, final double value) {
    return send(Action.hincrby, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("field", field)
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage hmset(final String key, final Map<String, String> hash) {
    KuzzleJSONObject  query = new KuzzleJSONObject();
    KuzzleJSONObject  values = new KuzzleJSONObject();
    query.put("_id", key);
    for(Map.Entry<String, String> entry : hash.entrySet()) {
      values.put(entry.getKey(), entry.getValue());
    }
    query.put("body", new KuzzleJSONObject().put("fields", values));
    return send(Action.hmset, query);
  }

  @Override
  public KuzzleMemoryStorage hset(final String key, final String field, final String value) {
    return send(Action.hset, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
          .put("field", field)
          .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage info(final String section) {
    return send(Action.info, new KuzzleJSONObject().put("body", new KuzzleJSONObject()
        .put("section", section)));
  }

  @Override
  public KuzzleMemoryStorage keys(final String pattern) {
    return send(Action.keys, new KuzzleJSONObject().put("body", new KuzzleJSONObject()
        .put("pattern", pattern)));
  }

  @Override
  public KuzzleMemoryStorage lastsave() {
    return send(Action.lastsave);
  }

  @Override
  public KuzzleMemoryStorage lindex(final String key, final long index) {
    return send(Action.lindex, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
          .put("idx", index)));
  }

  @Override
  public KuzzleMemoryStorage linsert(final String key, final Position where, final String pivot, final String value) {
    return send(Action.linsert, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("position", where.toString())
            .put("pivot", pivot)
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage lpush(final String key, final String... values) {
    return send(Action.lpush, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
          .put("values", values)));
  }

  @Override
  public KuzzleMemoryStorage lrange(final String key, final long start, final long end) {
    return send(Action.lrange, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("start", start)
            .put("end", end)));
  }

  @Override
  public KuzzleMemoryStorage lrem(final String key, final long count, final String value) {
    return send(Action.lrem, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("count", count)
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage lset(final String key, final long index, final String value) {
    return send(Action.lset, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("idx", index)
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage ltrim(final String key, final long start, final long end) {
    return send(Action.lrange, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("start", start)
            .put("stop", end)));
  }

  @Override
  public KuzzleMemoryStorage mset(final String... keysvalues) {
    return send(Action.mset, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
          .put("values", keysvalues)));
  }

  @Override
  public KuzzleMemoryStorage multi() {
    return send(Action.multi);
  }

  @Override
  public KuzzleMemoryStorage object(final ObjectCommand subcommand, final String args) {
    return send(Action.object, new KuzzleJSONObject()
      .put("body", new KuzzleJSONObject()
        .put("subcommand", subcommand.toString())
        .put("args", args)));
  }

  @Override
  public KuzzleMemoryStorage pexpire(final String key, final long milliseconds) {
    return send(Action.pexpire, new KuzzleJSONObject()
      .put("_id", key)
      .put("body", new KuzzleJSONObject()
        .put("milliseconds", milliseconds)));
  }

  @Override
  public KuzzleMemoryStorage pexpireat(final String key, final long timestamp) {
    return send(Action.pexpireat, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("timestamp", timestamp)));
  }

  @Override
  public KuzzleMemoryStorage pfadd(final String key, final String... elements) {
    return send(Action.pfadd, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("elements", elements)));
  }

  @Override
  public KuzzleMemoryStorage pfmerge(final String destKey, final String... sourceKeys) {
    return send(Action.pfmerge, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
            .put("destkey", destKey)
            .put("sourcekeys", sourceKeys)));
  }

  @Override
  public KuzzleMemoryStorage ping() {
    return send(Action.ping);
  }

  @Override
  public KuzzleMemoryStorage psetex(final String key, final long milliseconds, final String value) {
    return send(Action.psetex, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("milliseconds", milliseconds)
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage publish(final String channel, final String message) {
    return send(Action.publish, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
            .put("channel", channel)
            .put("message", message)));
  }

  @Override
  public KuzzleMemoryStorage randomkey() {
    return send(Action.randomkey);
  }

  @Override
  public KuzzleMemoryStorage rename(final String oldkey, final String newkey) {
    return send(Action.rename, new KuzzleJSONObject()
        .put("_id", oldkey)
        .put("body", new KuzzleJSONObject()
            .put("newkey", newkey)));
  }

  @Override
  public KuzzleMemoryStorage renamenx(final String oldkey, final String newkey) {
    return send(Action.renamenx, new KuzzleJSONObject()
        .put("_id", oldkey)
        .put("body", new KuzzleJSONObject()
            .put("newkey", newkey)));
  }

  @Override
  public KuzzleMemoryStorage restore(final String key, final long ttl, final String content) {
    return send(Action.restore, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("ttl", ttl)
            .put("content", content)));
  }

  @Override
  public KuzzleMemoryStorage rpoplpush(final String srckey, final String dstkey) {
    return send(Action.rpoplpush, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
            .put("source", srckey)
            .put("destination", dstkey)));
  }

  @Override
  public KuzzleMemoryStorage sadd(final String key, final String... members) {
    return send(Action.sadd, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("members", members)));
  }

  @Override
  public KuzzleMemoryStorage save() {
    return send(Action.save);
  }

  @Override
  public KuzzleMemoryStorage sdiffstore(final String dstkey, final String... keys) {
    return send(Action.sdiffstore, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
            .put("destination", dstkey)
            .put("keys", keys)));
  }

  @Override
  public KuzzleMemoryStorage set(final String key, final String value, final SetParams params) {
    KuzzleJSONObject kuzzleQuery;
    KuzzleJSONObject body = new KuzzleJSONObject();
    kuzzleQuery = new KuzzleJSONObject().put("_id", key);
    body.put("value", value);
    for (Map.Entry<String, Object> entry : params.getParams().entrySet()) {
      body.put(entry.getKey(), entry.getValue());
    }
    kuzzleQuery.put("body", body);
    return send(Action.set, kuzzleQuery);
  }

  @Override
  public KuzzleMemoryStorage setbit(final String key, final long offset, final Object value) {
    return send(Action.setbit, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("offset", offset)
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage setex(final String key, final int seconds, final String value) {
    return send(Action.setbit, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("seconds", seconds)
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage setrange(final String key, final long offset, final String value) {
    return send(Action.setrange, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("offset", offset)
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage sinterstore(final String dstkey, final String... keys) {
    return send(Action.sinterstore, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
            .put("destination", dstkey)
            .put("keys", keys)));
  }

  @Override
  public KuzzleMemoryStorage sismember(final String key, final String member) {
    return send(Action.sinterstore, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("member", member)));
  }

  @Override
  public KuzzleMemoryStorage smove(final String srckey, final String dstkey, final String member) {
    return send(Action.smove, new KuzzleJSONObject()
        .put("_id", srckey)
        .put("body", new KuzzleJSONObject()
            .put("destination", dstkey)
            .put("member", member)));
  }

  @Override
  public KuzzleMemoryStorage spop(final String key) {
    return send(Action.spop, new KuzzleJSONObject()
      .put("_id", key));
  }

  @Override
  public KuzzleMemoryStorage spop(final String key, final long count) {
    return send(Action.spop, new KuzzleJSONObject()
      .put("_id", key)
      .put("body", new KuzzleJSONObject()
        .put("count", count)));
  }

  @Override
  public KuzzleMemoryStorage srem(final String key, final String... members) {
    return send(Action.srem, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("members", members)));
  }

  @Override
  public KuzzleMemoryStorage sunionstore(final String dstkey, final String... keys) {
    return send(Action.sunionstore, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
            .put("destination", dstkey)
            .put("keys", keys)));
  }

  @Override
  public KuzzleMemoryStorage unwatch() {
    return send(Action.unwatch);
  }

  @Override
  public KuzzleMemoryStorage wait(final int replicas, final long timeout) {
    return send(Action.wait, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
            .put("numslaves", replicas)
            .put("timeout", timeout)));
  }

  @Override
  public KuzzleMemoryStorage zcount(final String key, final Object min, final Object max) {
    return send(Action.zcount, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
          .put("min", min)
          .put("max", max)));
  }

  @Override
  public KuzzleMemoryStorage zincrby(final String key, final double score, final String member) {
    return send(Action.zincrby, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("value", score)
            .put("member", member)));
  }

  @Override
  public KuzzleMemoryStorage zinterstore(final String destination, final String[] sets, final ZParams.Aggregate aggregate, final Object... weights) {
    return send(Action.zinterstore, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
          .put("destination", destination)
          .put("keys", sets)
          .put("aggregate", aggregate.toString())
          .put("weights", weights)));
  }

  @Override
  public KuzzleMemoryStorage zlexcount(final String key, final long min, final long max) {
    return send(Action.zlexcount, new KuzzleJSONObject()
      .put("_id", key)
      .put("body", new KuzzleJSONObject()
        .put("min", min)
        .put("max", max)));
  }

  @Override
  public KuzzleMemoryStorage zrange(final String key, final long start, final long end, final boolean withscores) {
    return send(Action.zrange, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("start", start)
            .put("stop", end)
            .put("withscores", withscores)));
  }

  @Override
  public KuzzleMemoryStorage zrangebylex(final String key, final long min, final long max, final long offset, final long count) {
    return send(Action.zrangebylex, new KuzzleJSONObject()
      .put("_id", key)
      .put("body", new KuzzleJSONObject()
        .put("min", min)
        .put("max", max)
        .put("offset", offset)
        .put("count", count)));
  }

  @Override
  public KuzzleMemoryStorage zrangebyscore(final String key, final long min, final long max, final boolean withscores, final long offset, final long count) {
    return send(Action.zrangebylex, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("min", min)
            .put("max", max)
            .put("withscores", withscores)
            .put("offset", offset)
            .put("count", count)));
  }

  @Override
  public KuzzleMemoryStorage zrem(final String key, final String... members) {
    return send(Action.zrem, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("members", members)));
  }

  @Override
  public KuzzleMemoryStorage zremrangebylex(final String key, final long min, final long max, final long offset, final long count) {
    return send(Action.zremrangebylex, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("min", min)
            .put("max", max)
            .put("offset", offset)
            .put("count", count)));
  }

  @Override
  public KuzzleMemoryStorage zrevrangebyscore(final String key, final long min, final long max, final boolean withscores, final long offset, final long count) {
    return send(Action.zrevrangebyscore, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("min", min)
            .put("max", max)
            .put("withscores", withscores)
            .put("offset", offset)
            .put("count", count)));
  }

  @Override
  public KuzzleMemoryStorage zrevrank(final String key, final String member) {
    return send(Action.zrevrank, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("member", member)));
  }

  // Unique Key argument methods

  private KuzzleMemoryStorage sendUniqueKeyArgument(final Action action, final String key) {
    return send(action, new KuzzleJSONObject()
        .put("_id", key));
  }

  @Override
  public KuzzleMemoryStorage decr(final String key) {
    return sendUniqueKeyArgument(Action.decr, key);
  }

  @Override
  public KuzzleMemoryStorage get(final String key) {
    return sendUniqueKeyArgument(Action.get, key);
  }

  @Override
  public KuzzleMemoryStorage dump(final String key) {
    return sendUniqueKeyArgument(Action.dump, key);
  }

  @Override
  public KuzzleMemoryStorage hgetall(final String key) {
    return sendUniqueKeyArgument(Action.hgetall, key);
  }

  @Override
  public KuzzleMemoryStorage hkeys(final String key) {
    return sendUniqueKeyArgument(Action.hkeys, key);
  }

  @Override
  public KuzzleMemoryStorage hlen(final String key) {
    return sendUniqueKeyArgument(Action.hlen, key);
  }

  @Override
  public KuzzleMemoryStorage hstrlen(final String key) {
    return sendUniqueKeyArgument(Action.hstrlen, key);
  }

  @Override
  public KuzzleMemoryStorage hvals(final String key) {
    return sendUniqueKeyArgument(Action.hvals, key);
  }

  @Override
  public KuzzleMemoryStorage incr(final String key) {
    return sendUniqueKeyArgument(Action.incr, key);
  }

  @Override
  public KuzzleMemoryStorage llen(final String key) {
    return sendUniqueKeyArgument(Action.llen, key);
  }

  @Override
  public KuzzleMemoryStorage lpop(final String key) {
    return sendUniqueKeyArgument(Action.lpop, key);
  }

  @Override
  public KuzzleMemoryStorage persist(final String key) {
    return sendUniqueKeyArgument(Action.persist, key);
  }

  @Override
  public KuzzleMemoryStorage pttl(final String key) {
    return sendUniqueKeyArgument(Action.pttl, key);
  }

  @Override
  public KuzzleMemoryStorage rpop(final String key) {
    return sendUniqueKeyArgument(Action.rpop, key);
  }

  @Override
  public KuzzleMemoryStorage scard(final String key) {
    return sendUniqueKeyArgument(Action.scard, key);
  }

  @Override
  public KuzzleMemoryStorage smembers(final String key) {
    return sendUniqueKeyArgument(Action.smembers, key);
  }

  @Override
  public KuzzleMemoryStorage strlen(final String key) {
    return sendUniqueKeyArgument(Action.strlen, key);
  }

  @Override
  public KuzzleMemoryStorage ttl(final String key) {
    return sendUniqueKeyArgument(Action.ttl, key);
  }

  @Override
  public KuzzleMemoryStorage type(final String key) {
    return sendUniqueKeyArgument(Action.type, key);
  }

  @Override
  public KuzzleMemoryStorage zcard(final String key) {
    return sendUniqueKeyArgument(Action.zcard, key);
  }

  @Override
  public KuzzleMemoryStorage getset(String key, String value) {
    return send(Action.getset, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage lpushx(String key, String value) {
    return send(Action.lpushx, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("value", value)));
  }

  // key key
  private KuzzleMemoryStorage keyKey(final Action action, final String... keys) {
    return send(action, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
            .put("keys", keys)));
  }

  @Override
  public KuzzleMemoryStorage del(final String... keys) {
    return keyKey(Action.del, keys);
  }

  @Override
  public KuzzleMemoryStorage mget(String... keys) {
    return keyKey(Action.mget, keys);
  }

  @Override
  public KuzzleMemoryStorage pfcount(String... keys) {
    return keyKey(Action.pfcount, keys);
  }

  @Override
  public KuzzleMemoryStorage exists(final String... keys) {
    return keyKey(Action.exists, keys);
  }

  @Override
  public KuzzleMemoryStorage sdiff(final String... keys) {
    return keyKey(Action.sdiff, keys);
  }

  @Override
  public KuzzleMemoryStorage sinter(final String... keys) {
    return keyKey(Action.sinter, keys);
  }

  @Override
  public KuzzleMemoryStorage sunion(final String... keys) {
    return keyKey(Action.sunion, keys);
  }

  @Override
  public KuzzleMemoryStorage watch(final String... keys) {
    return keyKey(Action.watch, keys);
  }

  @Override
  public KuzzleMemoryStorage incrby(String key, long value) {
    return send(Action.incrby, new KuzzleJSONObject()
      .put("_id", key)
      .put("body", new KuzzleJSONObject()
        .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage incrbyfloat(String key, double value) {
    return send(Action.incrbyfloat, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage brpop(final String[] args, long timeout) {
    return send(Action.brpop, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
            .put("src", args)
            .put("timeout", timeout)));
  }

  @Override
  public KuzzleMemoryStorage hget(final String key, final String field) {
    return send(Action.hget, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("field", field)));
  }

  @Override
  public KuzzleMemoryStorage hmget(final String key, final String... fields) {
    KuzzleJSONObject query = new KuzzleJSONObject();
    query.put("_id", key);
    query.put("body", new KuzzleJSONObject()
        .put("fields", fields));
    return send(Action.hmget, query);
  }

  @Override
  public KuzzleMemoryStorage hsetnx(final String key, final String field, final String value) {
    return send(Action.hsetnx, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("field", field)
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage msetnx(final String... keysvalues) {
    return send(Action.msetnx, new KuzzleJSONObject()
        .put("body", new KuzzleJSONObject()
            .put("values", keysvalues)));
  }

  @Override
  public KuzzleMemoryStorage rpush(final String key, final String... values) {
    return send(Action.rpush, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("values", values)));
  }

  @Override
  public KuzzleMemoryStorage hincrbyfloat(final String key, final String field, final double value) {
    return send(Action.hincrbyfloat, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("field", field)
            .put("value", value)));
  }

  @Override
  public KuzzleMemoryStorage srandmember(final String key, final long count) {
    return send(Action.srandmember, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("count", count)));
  }

  @Override
  public KuzzleMemoryStorage zrevrange(final String key, final long start, final long end, final boolean withscores) {
    return send(Action.zrevrange, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("start", start)
            .put("stop", end)
            .put("withscores", withscores)));
  }

  @Override
  public KuzzleMemoryStorage zscore(final String key, final String member) {
    return send(Action.zscore, new KuzzleJSONObject()
        .put("_id", key)
        .put("body", new KuzzleJSONObject()
            .put("member", member)));
  }

}