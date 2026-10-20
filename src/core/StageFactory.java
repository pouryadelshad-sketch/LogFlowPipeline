package core;

import java.util.Map;

public interface StageFactory {
    Stage create(Map<String, Object> params);
}