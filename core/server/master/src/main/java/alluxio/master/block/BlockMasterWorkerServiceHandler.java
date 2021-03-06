/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.master.block;

import alluxio.Constants;
import alluxio.RpcUtils;
import alluxio.exception.AlluxioException;
import alluxio.thrift.AlluxioTException;
import alluxio.thrift.BlockHeartbeatTOptions;
import alluxio.thrift.BlockHeartbeatTResponse;
import alluxio.thrift.BlockMasterWorkerService;
import alluxio.thrift.CommitBlockTOptions;
import alluxio.thrift.CommitBlockTResponse;
import alluxio.thrift.GetServiceVersionTOptions;
import alluxio.thrift.GetServiceVersionTResponse;
import alluxio.thrift.GetWorkerIdTOptions;
import alluxio.thrift.GetWorkerIdTResponse;
import alluxio.thrift.RegisterWorkerTOptions;
import alluxio.thrift.RegisterWorkerTResponse;
import alluxio.thrift.GetCachePermissionTOptions;
import alluxio.thrift.GetCachePermissionTResponse;
import alluxio.thrift.CacheFailedDecreaseTOptions;
import alluxio.thrift.CacheFailedDecreaseTResponse;
import alluxio.thrift.WorkerNetAddress;
import alluxio.wire.ThriftUtils;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * This class is a Thrift handler for block master RPCs invoked by an Alluxio worker.
 */
@NotThreadSafe // TODO(jiri): make thread-safe (c.f. ALLUXIO-1664)
public final class BlockMasterWorkerServiceHandler implements BlockMasterWorkerService.Iface {
  private static final Logger LOG = LoggerFactory.getLogger(BlockMasterWorkerServiceHandler.class);

  private final BlockMaster mBlockMaster;

  /**
   * Creates a new instance of {@link BlockMasterWorkerServiceHandler}.
   *
   * @param blockMaster the {@link BlockMaster} the handler uses internally
   */
  BlockMasterWorkerServiceHandler(BlockMaster blockMaster) {
    Preconditions.checkNotNull(blockMaster, "blockMaster");
    mBlockMaster = blockMaster;
  }

  @Override
  public GetServiceVersionTResponse getServiceVersion(GetServiceVersionTOptions options) {
    return new GetServiceVersionTResponse(Constants.BLOCK_MASTER_WORKER_SERVICE_VERSION);
  }

  @Override
  public BlockHeartbeatTResponse blockHeartbeat(final long workerId,
      final Map<String, Long> usedBytesOnTiers, final List<Long> removedBlockIds,
      final Map<String, List<Long>> addedBlocksOnTiers, BlockHeartbeatTOptions options)
      throws AlluxioTException {
    return RpcUtils.call(LOG, new RpcUtils.RpcCallable<BlockHeartbeatTResponse>() {
      @Override
      public BlockHeartbeatTResponse call() throws AlluxioException {
        return new BlockHeartbeatTResponse(mBlockMaster
            .workerHeartbeat(workerId, usedBytesOnTiers, removedBlockIds, addedBlocksOnTiers));
      }

      @Override
      public String toString() {
        return String.format("blockHeartbeat: workerId=%s, usedBytesOnTiers=%s, "
                + "removedBlockIds=%s, addedBlocksOnTiers=%s, options=%s", workerId,
            usedBytesOnTiers,
            removedBlockIds, addedBlocksOnTiers, options);
      }
    });
  }

  @Override
  public CommitBlockTResponse commitBlock(final long workerId, final long usedBytesOnTier,
      final String tierAlias, final long blockId, final long length, CommitBlockTOptions options)
      throws AlluxioTException {
    return RpcUtils.call(LOG, new RpcUtils.RpcCallableThrowsIOException<CommitBlockTResponse>() {
      @Override
      public CommitBlockTResponse call() throws AlluxioException, IOException {
        mBlockMaster.commitBlock(workerId, usedBytesOnTier, tierAlias, blockId, length);
        return new CommitBlockTResponse();
      }

      @Override
      public String toString() {
        return String.format("commitBlock: workerId=%s, usedBytesOnTiers=%s, tierAlias=%s, "
                + "blockId=%s, length=%s, options=%s", workerId, usedBytesOnTier, tierAlias,
            blockId,
            length, options);
      }
    });
  }

  @Override
  public GetCachePermissionTResponse getCachePermission(final long blockId,
      final String workerHostname,
      GetCachePermissionTOptions options) throws AlluxioTException {
    LOG.warn("pku-BlockMasterWorkerServiceHandler get cacheg permission from worker");
    return RpcUtils.call(LOG, new RpcUtils.RpcCallable<GetCachePermissionTResponse>() {
      @Override
      public GetCachePermissionTResponse call() throws AlluxioException {
        /* todo mBlockMaster.getCachePermission*/

        return new GetCachePermissionTResponse(
           mBlockMaster.getCachePermission(blockId, workerHostname)
        );
      }

      @Override
      public String toString() {
        return String.format("getCachePermission: blockId=%s, workerHostname=%s, "
                + "options=%s", blockId, workerHostname, options);
      }
    });
  }

  @Override
  public CacheFailedDecreaseTResponse cacheFailedDecrease(final long blockId,
      final String workerHostname,
      CacheFailedDecreaseTOptions options) throws AlluxioTException {
    LOG.warn(String
            .format("pku-BlockMasterWorkerServiceHandler get cacheFailedDecrease from worker"));
    return RpcUtils.call(LOG, new RpcUtils.RpcCallable<CacheFailedDecreaseTResponse>() {
      @Override
      public CacheFailedDecreaseTResponse call() throws AlluxioException {
          /**modify**/
          mBlockMaster.cacheFailedDecrease(blockId, workerHostname);
          return new CacheFailedDecreaseTResponse();
      }

      @Override
      public String toString() {
        return String.format("cacheFailedDecrease: blockId=%s, workerHostname=%s, "
                + " options=%s", blockId, workerHostname, options);
      }
    });
  }

  @Override
  public GetWorkerIdTResponse getWorkerId(final WorkerNetAddress workerNetAddress,
      GetWorkerIdTOptions options) throws AlluxioTException {
    return RpcUtils.call(LOG, new RpcUtils.RpcCallable<GetWorkerIdTResponse>() {
      @Override
      public GetWorkerIdTResponse call() throws AlluxioException {
        return new GetWorkerIdTResponse(
            mBlockMaster.getWorkerId(ThriftUtils.fromThrift((workerNetAddress))));
      }

      @Override
      public String toString() {
        return String
            .format("getWorkerId: workerNetAddress=%s, options=%s", workerNetAddress, options);
      }
    });
  }

  @Override
  public RegisterWorkerTResponse registerWorker(final long workerId,
      final List<String> storageTiers, final Map<String, Long> totalBytesOnTiers,
      final Map<String, Long> usedBytesOnTiers, final Map<String, List<Long>> currentBlocksOnTiers,
      RegisterWorkerTOptions options) throws AlluxioTException {
    return RpcUtils.call(LOG, new RpcUtils.RpcCallable<RegisterWorkerTResponse>() {
      @Override
      public RegisterWorkerTResponse call() throws AlluxioException {
        mBlockMaster.workerRegister(workerId, storageTiers, totalBytesOnTiers, usedBytesOnTiers,
            currentBlocksOnTiers);
        return new RegisterWorkerTResponse();
      }

      @Override
      public String toString() {
        return String
            .format("registerWorker: workerId=%s, storageTiers=%s, totalBytesOnTiers=%s,"
                + "usedBytesOnTiers=%s, currentBlocksOnTiers=%s, options=%s", workerId,
            storageTiers, totalBytesOnTiers, usedBytesOnTiers, currentBlocksOnTiers, options);
      }
    });
  }
}

