Pod::Spec.new do |s|
  s.name             = 'LiveMutiHostCore'
  s.version          = '0.0.1'
  s.summary          = 'LiveMutiHostCore'
  
  s.description      = <<-DESC
  TODO: Add long description of the pod here.
  DESC
  
  s.homepage         = 'https://github.com'
  s.license          = { :type => 'MIT', :file => 'LICENSE' }
  s.author           = { 'ZYP' => 'zhuyuping@agora.io' }
  s.source           = { :git => 'https://github.com', :tag => s.version.to_s }
  
  s.ios.deployment_target = '11.0'
  s.source_files = '**/*/*.swift'
  s.dependency 'AgoraLog'
  s.dependency 'Toast-Swift'
  s.dependency 'AgoraRtcEngine_iOS'
end
