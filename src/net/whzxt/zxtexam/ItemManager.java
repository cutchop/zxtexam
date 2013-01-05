package net.whzxt.zxtexam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ItemManager {
	public static interface OnStatusChange {
		public abstract void onFault(int index);

		public abstract void onStop();
	}

	private Timer _timer;
	private final int Period = 100;
	public ArrayList<Action> _listActions;
	public Boolean _pause = true;
	private int _timeout;
	private OnStatusChange _onStatusChange;
	private HashMap<Integer, Integer> _hashdata;
	private Metadata _md;
	private int _startAngle; // 开始时的GPS角度
	private int _step;
	private Boolean _stepfinish;
	private int _fenshu = 100;

	public ItemManager(OnStatusChange osc, Metadata md) {
		_md = md;
		_onStatusChange = osc;
		_hashdata = new HashMap<Integer, Integer>();
		_pause = true;
		_timer = new Timer();
		_timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!_pause && _timeout > 0) {
					_timeout--;
					if (_timeout == 0) {
						// 扣分
						for (int i = 0; i < _listActions.size(); i++) {
							if (_listActions.get(i).Step == _step && !_listActions.get(i).IsOK) {
								if (_listActions.get(i).Dataid < 20) {
									if (Math.abs(_listActions.get(i).Times) > 1) {
										_onStatusChange.onFault(i);
										_fenshu = _fenshu - _listActions.get(i).Fenshu;
										if (_fenshu < 90) {
											break;
										}
									}
								} else {
									if (Math.abs(_listActions.get(i).Min) > 0 && _listActions.get(i).Max == 0) {
										_onStatusChange.onFault(i);
										_fenshu = _fenshu - _listActions.get(i).Fenshu;
										if (_fenshu < 90) {
											break;
										}
									}
								}
							}
						}
						// 结束
						Stop();
						return;
					} else {
						_stepfinish = true;
						for (int i = 0; i < _listActions.size(); i++) {
							if (_listActions.get(i).Step == _step && !_listActions.get(i).IsOK) {
								if (_listActions.get(i).Dataid < 20) {
									if (Math.abs(_listActions.get(i).Times) > 1) {
										_stepfinish = false;
										break;
									}
								} else {
									if (Math.abs(_listActions.get(i).Min) > 0 && _listActions.get(i).Max == 0) {
										_stepfinish = false;
										break;
									}
								}
							}
						}
						if (_stepfinish) {
							_step++;
							Boolean hasStep = false;
							for (int i = 0; i < _listActions.size(); i++) {
								if (_listActions.get(i).Step == _step) {
									hasStep = true;
									break;
								}
							}
							if (!hasStep) {
								for (int i = 0; i < _listActions.size(); i++) {
									if (_listActions.get(i).Step == _step - 1) {
										if (_listActions.get(i).Dataid < 20) {
											if (Math.abs(_listActions.get(i).Times) == 1) {
												_step--;
												hasStep = true;
												break;
											}
										} else {
											if (Math.abs(_listActions.get(i).Max) > 0) {
												_step--;
												hasStep = true;
												break;
											}
										}
									}
								}
							}
							if (!hasStep) {
								Stop();
								return;
							}
						}
						for (int i = 0; i < _listActions.size(); i++) {
							if (_listActions.get(i).Step == _step) {
								if (_listActions.get(i).Dataid < 20) {
									switch (_listActions.get(i).Times) {
									case 1:
										if (_md.getData(_listActions.get(i).Dataid) == 1) {
											if (_listActions.get(i).IsOK) {
												_listActions.get(i).IsOK = false;
												_onStatusChange.onFault(i);
												_fenshu = _fenshu - _listActions.get(i).Fenshu;
												if (_fenshu < 90) {
													Stop();
												}
											}
										}
										break;
									case -1:
										if (_md.getData(_listActions.get(i).Dataid) == 0) {
											if (_listActions.get(i).IsOK) {
												_listActions.get(i).IsOK = false;
												_onStatusChange.onFault(i);
												_fenshu = _fenshu - _listActions.get(i).Fenshu;
												if (_fenshu < 90) {
													Stop();
												}
											}
										}
										break;
									case 2:
										if (_md.getData(_listActions.get(i).Dataid) == 1) {
											_listActions.get(i).IsOK = true;
										}
										break;
									case -2:
										if (_md.getData(_listActions.get(i).Dataid) == 0) {
											_listActions.get(i).IsOK = true;
										}
										break;
									case 3:
										if (_hashdata.get(_listActions.get(i).Dataid) == null) {
											_hashdata.put(_listActions.get(i).Dataid, _md.getData(_listActions.get(i).Dataid));
										} else {
											_hashdata.put(_listActions.get(i).Dataid, _hashdata.get(_listActions.get(i).Dataid) + _md.getData(_listActions.get(i).Dataid));
										}
										if (_hashdata.get(_listActions.get(i).Dataid) >= 1 * (1000 / Period)) {
											_listActions.get(i).IsOK = true;
										}
										break;
									case -3:
										if (_hashdata.get(_listActions.get(i).Dataid) == null) {
											_hashdata.put(_listActions.get(i).Dataid, _md.getData(_listActions.get(i).Dataid) == 1 ? 0 : 1);
										} else {
											_hashdata.put(_listActions.get(i).Dataid, _hashdata.get(_listActions.get(i).Dataid) + (_md.getData(_listActions.get(i).Dataid) == 1 ? 0 : 1));
										}
										if (_hashdata.get(_listActions.get(i).Dataid) >= 1 * (1000 / Period)) {
											_listActions.get(i).IsOK = true;
										}
										break;
									case 4:
										if (_hashdata.get(_listActions.get(i).Dataid) == null) {
											_hashdata.put(_listActions.get(i).Dataid, _md.getData(_listActions.get(i).Dataid));
										} else {
											_hashdata.put(_listActions.get(i).Dataid, _hashdata.get(_listActions.get(i).Dataid) + _md.getData(_listActions.get(i).Dataid));
										}
										if (_hashdata.get(_listActions.get(i).Dataid) >= 3 * (1000 / Period)) {
											_listActions.get(i).IsOK = true;
										}
										break;
									case -4:
										if (_hashdata.get(_listActions.get(i).Dataid) == null) {
											_hashdata.put(_listActions.get(i).Dataid, _md.getData(_listActions.get(i).Dataid) == 1 ? 0 : 1);
										} else {
											_hashdata.put(_listActions.get(i).Dataid, _hashdata.get(_listActions.get(i).Dataid) + (_md.getData(_listActions.get(i).Dataid) == 1 ? 0 : 1));
										}
										if (_hashdata.get(_listActions.get(i).Dataid) >= 3 * (1000 / Period)) {
											_listActions.get(i).IsOK = true;
										}
										break;
									default:
										break;
									}
								} else if (_listActions.get(i).Dataid == 31) {
									if (_startAngle == -1) {
										_startAngle = _md.getData(31);
									} else {
										int angle = _md.getData(31) - _startAngle;
										if (angle > 200) {
											angle = (360 - angle) * -1;
										}
										if (Math.abs(_listActions.get(i).Max) > 0) {
											if ((_listActions.get(i).Max > 0 && angle > _listActions.get(i).Max) || (_listActions.get(i).Max < 0 && angle < _listActions.get(i).Max)) {
												if (_listActions.get(i).IsOK) {
													_listActions.get(i).IsOK = false;
													_onStatusChange.onFault(i);
													_fenshu = _fenshu - _listActions.get(i).Fenshu;
													if (_fenshu < 90) {
														Stop();
														break;
													}
												}
											}
										} else if (Math.abs(_listActions.get(i).Min) > 0) {
											if ((_listActions.get(i).Min > 0 && angle > _listActions.get(i).Min) || (_listActions.get(i).Min < 0 && angle < _listActions.get(i).Min)) {
												_listActions.get(i).IsOK = true;
											}
										}
									}
								} else {
									if (_listActions.get(i).Max > 0 && _listActions.get(i).Min > 0) {
										if (_md.getData(_listActions.get(i).Dataid) > _listActions.get(i).Max || _md.getData(_listActions.get(i).Dataid) < _listActions.get(i).Min) {
											if (_listActions.get(i).IsOK) {
												_listActions.get(i).IsOK = false;
												_onStatusChange.onFault(i);
												_fenshu = _fenshu - _listActions.get(i).Fenshu;
												if (_fenshu < 90) {
													Stop();
													break;
												}
											}
										}
									} else {
										if (_listActions.get(i).Max > 0) {
											if (_md.getData(_listActions.get(i).Dataid) > _listActions.get(i).Max) {
												if (_listActions.get(i).IsOK) {
													_listActions.get(i).IsOK = false;
													_onStatusChange.onFault(i);
													_fenshu = _fenshu - _listActions.get(i).Fenshu;
													if (_fenshu < 90) {
														Stop();
														break;
													}
												}
											}
										} else if (_listActions.get(i).Min > 0) {
											if (_md.getData(_listActions.get(i).Dataid) > _listActions.get(i).Min) {
												_listActions.get(i).IsOK = true;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}, 1000, Period);
	}

	public void Start() {
		_step = 1;
		_startAngle = -1;
		_hashdata.clear();
		_pause = false;
	}

	public void Stop() {
		_pause = true;
		_onStatusChange.onStop();
	}

	public void Destroy() {
		_timer.cancel();
		_timer = null;
	}

	public void setFenshu(int fenshu) {
		_fenshu = fenshu;
	}

	public void setTimeout(int timeout) {
		_timeout = timeout * (1000 / Period);
	}
}
