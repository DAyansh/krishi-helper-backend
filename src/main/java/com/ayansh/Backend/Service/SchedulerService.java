//package com.ayansh.Backend.Service;
//
//
//import com.ayansh.Backend.Model.NdviHistory;
//import com.ayansh.Backend.Model.Polygon;
//import com.ayansh.Backend.Repository.NdviHistoryRepo;
//import com.ayansh.Backend.Repository.PolygonRepo;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.util.List;
//
//@Service
//public class SchedulerService {
//
//    @Autowired
//        private PolygonRepo polygonRepo;
//
//    @Autowired
//        private AgroService agroService;
//
//    @Autowired
//        private NdviHistoryRepo ndviRepo;
//
//    @Autowired
//        private final ObjectMapper mapper = new ObjectMapper();
//
//        public SchedulerService(PolygonRepo polygonRepo, AgroService agroService, NdviHistoryRepo ndviRepo) {
//            this.polygonRepo = polygonRepo;
//            this.agroService = agroService;
//            this.ndviRepo = ndviRepo;
//        }
//
//
//        @Scheduled(cron = "0 30 3 * * *")
//        public void fetchDailyNdviForAll() {
//            List<Polygon> polys = polygonRepo.findAll();
//            for (var p : polys) {
//                try {
//                    String resp = agroService.getNdvi(p.getAgroPolygonId());
//                    JsonNode node = mapper.readTree(resp);
//                    // node may be array of values
//                    if (node.isArray()) {
//                        for (JsonNode v : node) {
//                            long ts = v.path("dt").asLong();
//                            double ndvi = v.path("ndvi").asDouble();
//                            NdviHistory h = NdviHistory.builder()
//                                    .agroPolygonId(p.getAgroPolygonId())
//                                    .date(Instant.ofEpochSecond(ts))
//                                    .ndvi(ndvi)
//                                    .build();
//                            ndviRepo.save(h);
//                        }
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//    }
//
