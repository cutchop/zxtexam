package net.whzxt.zxtexam;

public class SignalAction extends BaseAction {

	private int _total = 0; // 累计出现的次数

	public SignalAction(Metadata md, int dataid, int times, int min, int max) {
		super(md, dataid, times, min, max);
	}

	@Override
	public Boolean CheckOK(int step) {
		if (Step != step) {
			return true;
		}
		if (Math.abs(_times) == 1) {
			return true;
		}
		if (_isOK) {
			return true;
		}
		switch (_times) {
		case 2:
			if (_md.getData(_dataid) == 1) {// 等于1就通过
				_isOK = true;
			}
			break;
		case -2:
			if (_md.getData(_dataid) == 0) {// 等于0就通过
				_isOK = true;
			}
			break;
		case 3:
			if (_md.getData(_dataid) == 1) {// 等于1就累计1次
				_total++;
				if (_total >= (1000 / Metadata.PERIOD)) {// 累计次数大于1秒就通过
					_isOK = true;
				}
			}
			break;
		case -3:
			if (_md.getData(_dataid) == 0) {// 等于0就累计1次
				_total++;
				if (_total >= (1000 / Metadata.PERIOD)) {// 累计次数大于1秒就通过
					_isOK = true;
				}
			}
			break;
		case 4:
			if (_md.getData(_dataid) == 1) {// 等于1就累计1次
				_total++;
				if (_total >= (3000 / Metadata.PERIOD)) {// 累计次数大于1秒就通过
					_isOK = true;
				}
			}
			break;
		case -4:
			if (_md.getData(_dataid) == 0) {// 等于0就累计1次
				_total++;
				if (_total >= (3000 / Metadata.PERIOD)) {// 累计次数大于1秒就通过
					_isOK = true;
				}
			}
			break;
		default:
			break;
		}
		return _isOK;
	}

	@Override
	public Boolean CheckError(int step) {
		if (Step != step) {
			return false;
		}
		if (Math.abs(_times) > 1) {
			return false;
		}
		if (_isError) {
			return false;
		}
		if (_times == 1) {
			if (_md.getData(_dataid) == 1) {//等于1就扣分
				_isError = true;
			}
		} else if (_times == -1) {
			if (_md.getData(_dataid) == 0) {//等于0就扣分
				_isError = true;
			}
		}
		return _isError;
	}
}
