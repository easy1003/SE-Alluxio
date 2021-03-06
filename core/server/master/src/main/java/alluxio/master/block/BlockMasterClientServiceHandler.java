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
import alluxio.RpcUtils.RpcCallable;
import alluxio.RpcUtils.RpcCallableThrowsIOException;
import alluxio.exception.AlluxioException;
import alluxio.exception.status.AlluxioStatusException;
import alluxio.thrift.AlluxioTException;
import alluxio.thrift.BlockMasterClientService;

import alluxio.thrift.GetBlockInfoTOptions;
import alluxio.thrift.GetBlockInfoTResponse;

import alluxio.thrift.GetCapacityBytesTOptions;
import alluxio.thrift.GetCapacityBytesTResponse;
import alluxio.thrift.GetServiceVersionTOptions;
import alluxio.thrift.GetServiceVersionTResponse;
import alluxio.thrift.GetUsedBytesTOptions;
import alluxio.thrift.GetUsedBytesTResponse;
import alluxio.thrift.GetWorkerInfoListTOptions;
import alluxio.thrift.GetWorkerInfoListTResponse;
import alluxio.thrift.BlockChecksumStoreTOptions;
import alluxio.thrift.BlockChecksumStoreTResponse;
import alluxio.thrift.BlockConsitencyCheckTResponse;
import alluxio.thrift.BlockConsitencyCheckTOptions;
import alluxio.thrift.WorkerInfo;
import alluxio.wire.ThriftUtils;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * This class is a Thrift handler for block master RPCs invoked by an Alluxio client.
 */
@NotThreadSafe // TODO(jiri): make thread-safe (c.f. ALLUXIO-1664)
public final class BlockMasterClientServiceHandler implements BlockMasterClientService.Iface {
  private static final Logger LOG = LoggerFactory.getLogger(BlockMasterClientServiceHandler.class);

  private final BlockMaster mBlockMaster;

  /**
   * Creates a new instance of {@link BlockMasterClientServiceHandler}.
   *
   * @param blockMaster the {@link BlockMaster} the handler uses internally
   */
  BlockMasterClientServiceHandler(BlockMaster blockMaster) {
    Preconditions.checkNotNull(blockMaster, "blockMaster");
    mBlockMaster = blockMaster;
  }

  @Override
  public GetServiceVersionTResponse getServiceVersion(GetServiceVersionTOptions options) {
    return new GetServiceVersionTResponse(Constants.BLOCK_MASTER_CLIENT_SERVICE_VERSION);
  }

  @Override
  public GetWorkerInfoListTResponse getWorkerInfoList(GetWorkerInfoListTOptions options)
      throws AlluxioTException {
    return RpcUtils.call(LOG, new RpcCallableThrowsIOException<GetWorkerInfoListTResponse>() {
      @Override
      public GetWorkerInfoListTResponse call() throws AlluxioException, AlluxioStatusException {
        List<WorkerInfo> workerInfos = new ArrayList<>();
        for (alluxio.wire.WorkerInfo workerInfo : mBlockMaster.getWorkerInfoList()) {
          workerInfos.add(ThriftUtils.toThrift(workerInfo));
        }
        return new GetWorkerInfoListTResponse(workerInfos);
      }

      @Override
      public String toString() {
        return String.format("getWorkerInfoList: options=%s", options);
      }
    });
  }

  @Override
  public GetCapacityBytesTResponse getCapacityBytes(GetCapacityBytesTOptions options)
      throws AlluxioTException {
    return RpcUtils.call(LOG, new RpcCallable<GetCapacityBytesTResponse>() {
      @Override
      public GetCapacityBytesTResponse call() throws AlluxioException {
        return new GetCapacityBytesTResponse(mBlockMaster.getCapacityBytes());
      }

      @Override
      public String toString() {
        return String.format("getCapacityBytes: options=%s", options);
      }
    });
  }

  @Override
  public GetUsedBytesTResponse getUsedBytes(GetUsedBytesTOptions options) throws AlluxioTException {
    return RpcUtils.call(LOG, new RpcCallable<GetUsedBytesTResponse>() {
      @Override
      public GetUsedBytesTResponse call() throws AlluxioException {
        return new GetUsedBytesTResponse(mBlockMaster.getUsedBytes());
      }

      @Override
      public String toString() {
        return String.format("getUsedBytes: options=%s", options);
      }
    });
  }

  @Override
  public BlockChecksumStoreTResponse blockChecksumStore(long blockId,
      String digest, BlockChecksumStoreTOptions options)
      throws AlluxioTException {
    return RpcUtils.call(LOG, new RpcCallable<BlockChecksumStoreTResponse>() {
      @Override
      public BlockChecksumStoreTResponse call() throws AlluxioException {
        mBlockMaster.blockChecksumStore(blockId, digest);
        return new BlockChecksumStoreTResponse();
      }

      @Override
      public String toString() {
        return String.format("blockChecksumStore: options=%s", options);
      }
    });
  }

  @Override
  public BlockConsitencyCheckTResponse blockConsitencyCheck(long blockId,
      String digest, BlockConsitencyCheckTOptions options)
      throws AlluxioTException {
    return RpcUtils.call(LOG, new RpcCallable<BlockConsitencyCheckTResponse>() {
      @Override
      public BlockConsitencyCheckTResponse call() throws AlluxioException {
        return new BlockConsitencyCheckTResponse(blockId,
          mBlockMaster.blockConsistencyCheck(blockId, digest));
      }

      @Override
      public String toString() {
        return String.format("blockConsistencyCheck: options=%s", options);
      }
    });

  }

  @Override
  public GetBlockInfoTResponse getBlockInfo(final long blockId, GetBlockInfoTOptions options)
      throws AlluxioTException {
    return RpcUtils.call(LOG, new RpcCallableThrowsIOException<GetBlockInfoTResponse>() {
      @Override
      public GetBlockInfoTResponse call() throws AlluxioException, AlluxioStatusException {
        return new GetBlockInfoTResponse(ThriftUtils.toThrift(mBlockMaster.getBlockInfo(blockId)));
      }

      @Override
      public String toString() {
        return String.format("getBlockInfo: blockId=%s, options=%s", blockId, options);
      }
    });
  }
}
